package org.spacelab.housingutilitiessystemadmin.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.spacelab.housingutilitiessystemadmin.entity.Chairman;
import org.spacelab.housingutilitiessystemadmin.exception.OperationException;
import org.spacelab.housingutilitiessystemadmin.mappers.ChairmanMapper;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanRequest;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponse;
import org.spacelab.housingutilitiessystemadmin.models.chairman.ChairmanResponseTable;
import org.spacelab.housingutilitiessystemadmin.models.filters.chairman.ChairmanRequestTable;
import org.spacelab.housingutilitiessystemadmin.repository.ChairmanRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChairmanService {

    private final ChairmanRepository chairmanRepository;
    private final ChairmanMapper chairmanMapper;
    private final FileService fileService;

    public Optional<Chairman> findById(String id) {
        return chairmanRepository.findById(id);
    }

    public Chairman save(Chairman chairman) {
        return chairmanRepository.save(chairman);
    }

    public List<Chairman> findAll() {
        return chairmanRepository.findAll();
    }

    public void deleteById(String id) {
        chairmanRepository.deleteById(id);
    }

    public List<Chairman> saveAll(List<Chairman> chairmen) {
        return chairmanRepository.saveAll(chairmen);
    }

    public void deleteAll() {
        chairmanRepository.deleteAll();
    }

    public ChairmanResponse getChairmanById(String id) {
        Chairman chairman = chairmanRepository.findById(id)
                .orElseThrow(() -> new OperationException("получении председателя",
                        "Председатель с ID " + id + " не найден", HttpStatus.NOT_FOUND));
        log.info("Председатель с ID {} успешно получен", id);
        return chairmanMapper.mapChairmanToResponse(chairman);
    }

    public ChairmanResponse createChairman(ChairmanRequest chairmanRequest) {
        Optional<Chairman> existingByEmail = chairmanRepository.findByEmail(chairmanRequest.getEmail());
        if (existingByEmail.isPresent()) {
            throw new OperationException("создании председателя",
                    "Председатель с email " + chairmanRequest.getEmail() + " уже существует", HttpStatus.CONFLICT);
        }

        Optional<Chairman> existingByLogin = chairmanRepository.findByLogin(chairmanRequest.getLogin());
        if (existingByLogin.isPresent()) {
            throw new OperationException("создании председателя",
                    "Председатель с логином " + chairmanRequest.getLogin() + " уже существует", HttpStatus.CONFLICT);
        }

        Chairman chairman = chairmanMapper.toEntity(chairmanRequest);
        
        if (chairmanRequest.getPassword() != null && !chairmanRequest.getPassword().isEmpty()) {
            chairman.setPassword(chairmanRequest.getPassword());
        }
        
        if (chairmanRequest.getPhotoFile() != null && !chairmanRequest.getPhotoFile().isEmpty()) {
            try {
                String photoPath = fileService.uploadFile(chairmanRequest.getPhotoFile());
                chairman.setPhoto(photoPath);
                log.info("Фото для председателя успешно загружено: {}", photoPath);
            } catch (Exception e) {
                log.error("Ошибка при загрузке фото председателя", e);
                throw new OperationException("создании председателя",
                        "Ошибка при загрузке фото: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        Chairman savedChairman = chairmanRepository.save(chairman);
        log.info("Председатель с ID {} успешно создан", savedChairman.getId());
        return chairmanMapper.mapChairmanToResponse(savedChairman);
    }

    public ChairmanResponse updateChairman(String id, ChairmanRequest chairmanRequest) {
        Chairman chairman = chairmanRepository.findById(id)
                .orElseThrow(() -> new OperationException("обновлении председателя",
                        "Председатель с ID " + id + " не найден"));

        chairmanMapper.partialUpdate(chairmanRequest, chairman);
        
        if (chairmanRequest.getPassword() != null && !chairmanRequest.getPassword().isEmpty()) {
            chairman.setPassword(chairmanRequest.getPassword());
        }
        
        if (chairmanRequest.getPhotoFile() != null && !chairmanRequest.getPhotoFile().isEmpty()) {
            try {
                if (chairman.getPhoto() != null && !chairman.getPhoto().isEmpty()) {
                    fileService.deleteFile(chairman.getPhoto());
                }
                
                String photoPath = fileService.uploadFile(chairmanRequest.getPhotoFile());
                chairman.setPhoto(photoPath);
                log.info("Фото для председателя с ID {} успешно обновлено: {}", id, photoPath);
            } catch (Exception e) {
                log.error("Ошибка при обновлении фото председателя с ID {}", id, e);
                throw new OperationException("обновлении председателя",
                        "Ошибка при загрузке фото: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        
        Chairman updatedChairman = chairmanRepository.save(chairman);
        log.info("Председатель с ID {} успешно обновлен", updatedChairman.getId());
        return chairmanMapper.mapChairmanToResponse(updatedChairman);
    }

    public boolean deleteChairman(String id) {
        if (!chairmanRepository.existsById(id)) {
            throw new OperationException("удалении председателя",
                    "Председатель с ID " + id + " не найден");
        }
        chairmanRepository.deleteById(id);
        log.info("Председатель с ID {} успешно удален", id);
        return true;
    }

    public Page<ChairmanResponseTable> getChairmenTable(ChairmanRequestTable chairmanRequestTable) {
        Page<Chairman> chairmen = getChairman(chairmanRequestTable);
        Page<ChairmanResponseTable> chairmanResponses = chairmanMapper.toResponseTablePage(chairmen);
        log.info("Получена страница председателей: страница {}, размер {}", 
                chairmanRequestTable.getPage(), chairmanRequestTable.getSize());
        return chairmanResponses;
    }

    public Page<Chairman> getChairman(ChairmanRequestTable chairmanRequestTable) {
        return chairmanRepository.findChairmenWithFilters(chairmanRequestTable);
    }

    public List<ChairmanResponse> searchByName(String name) {
        List<Chairman> chairmen;
        if (name == null || name.trim().isEmpty()) {
            chairmen = chairmanRepository.findAll();
        } else {
            chairmen = chairmanRepository.findByFullNameContaining(name.trim());
        }
        return chairmanMapper.toResponseList(chairmen);
    }

    @Async
    public CompletableFuture<ChairmanResponse> findByIdAsync(String id) {
        return CompletableFuture.completedFuture(findById(id)).thenApply(chairmanOpt -> {
            if (chairmanOpt.isEmpty()) {
                throw new OperationException("получении председателя",
                        "Председатель с ID " + id + " не найден", HttpStatus.NOT_FOUND);
            }
            return chairmanMapper.mapChairmanToResponse(chairmanOpt.get());
        });
    }

    @Async
    public CompletableFuture<Optional<Chairman>> findByEmailAsync(String email) {
        return CompletableFuture.completedFuture(chairmanRepository.findByEmail(email));
    }

    @Async
    public CompletableFuture<ChairmanResponse> createChairmanAsync(@Valid ChairmanRequest chairmanRequest) {
        return CompletableFuture.completedFuture(createChairman(chairmanRequest));
    }

    @Async
    public CompletableFuture<ChairmanResponse> updateChairmanAsync(@Valid ChairmanRequest chairmanRequest) {
        return CompletableFuture.completedFuture(updateChairman(chairmanRequest.getId(), chairmanRequest));
    }

    @Async
    public CompletableFuture<Boolean> deleteByIdAsync(String id) {
        Boolean deleted = deleteChairman(id);
        return CompletableFuture.completedFuture(deleted);
    }

    @Async
    public CompletableFuture<Page<ChairmanResponseTable>> getPageableChairmenAsync(
            int page, int size, String lastName, String firstName, String middleName,
            String phone, String email, String login, String status) {

        ChairmanRequestTable chairmanRequestTable = new ChairmanRequestTable();
        chairmanRequestTable.setPage(page);
        chairmanRequestTable.setSize(size);

        String fullNameSearch = "";
        if (lastName != null && !lastName.trim().isEmpty()) {
            fullNameSearch += lastName.trim() + " ";
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            fullNameSearch += firstName.trim() + " ";
        }
        if (middleName != null && !middleName.trim().isEmpty()) {
            fullNameSearch += middleName.trim();
        }
        if (!fullNameSearch.trim().isEmpty()) {
            chairmanRequestTable.setFullName(fullNameSearch.trim());
        }

        if (phone != null && !phone.trim().isEmpty()) {
            chairmanRequestTable.setPhone(phone.trim());
        }

        if (email != null && !email.trim().isEmpty()) {
            chairmanRequestTable.setEmail(email.trim());
        }

        if (login != null && !login.trim().isEmpty()) {
            chairmanRequestTable.setLogin(login.trim());
        }

        if (status != null && !status.trim().isEmpty()) {
            chairmanRequestTable.setStatus(status.trim());
        }

        return CompletableFuture.completedFuture(getChairmenTable(chairmanRequestTable));
    }
}