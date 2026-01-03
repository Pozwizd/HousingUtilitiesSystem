package org.spacelab.housingutilitiessystemadmin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spacelab.housingutilitiessystemadmin.entity.User;
import org.spacelab.housingutilitiessystemadmin.entity.location.City;
import org.spacelab.housingutilitiessystemadmin.entity.location.Status;
import org.spacelab.housingutilitiessystemadmin.entity.location.Street;
import org.spacelab.housingutilitiessystemadmin.models.PageResponse;
import org.spacelab.housingutilitiessystemadmin.models.filters.user.UserRequestTable;
import org.spacelab.housingutilitiessystemadmin.models.user.UserResponseTable;
import org.spacelab.housingutilitiessystemadmin.repository.UserRepository;
import org.spacelab.housingutilitiessystemadmin.search.UserSearchDocument;
import org.spacelab.housingutilitiessystemadmin.search.UserSearchRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSearchService Tests")
class UserSearchServiceTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private UserSearchRepository userSearchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IndexOperations indexOperations;

    @Mock
    private SearchHits<UserSearchDocument> searchHits;

    @Mock
    private SearchHit<UserSearchDocument> searchHit;

    @InjectMocks
    private UserSearchService userSearchService;

    private UserSearchDocument testDocument;
    private User testUser;

    @BeforeEach
    void setUp() {
        testDocument = UserSearchDocument.builder()
                .id("user-id-123")
                .firstName("John")
                .lastName("Doe")
                .middleName("Middle")
                .phone("1234567890")
                .email("john@test.com")
                .fullName("John Doe")
                .cityName("City")
                .streetName("Street")
                .houseNumber("123")
                .apartmentNumber("1")
                .accountNumber("ACC123")
                .status("ACTIVE")
                .build();

        testUser = new User();
        testUser.setId("user-id-123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setMiddleName("Middle");
        testUser.setPhone("1234567890");
        testUser.setEmail("john@test.com");
        testUser.setHouseNumber("123");
        testUser.setApartmentNumber("1");
        testUser.setAccountNumber("ACC123");
        testUser.setStatus(Status.ACTIVE);
    }

    @Nested
    @DisplayName("getUsersTable Tests")
    class GetUsersTableTests {
        @Test
        @DisplayName("Should get users table when index exists")
        void getUsersTable_shouldReturnTable_whenIndexExists() {
            UserRequestTable requestTable = new UserRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(true);
            when(elasticsearchOperations.search(any(Query.class), eq(UserSearchDocument.class))).thenReturn(searchHits);
            when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);
            when(searchHit.getContent()).thenReturn(testDocument);

            PageResponse<UserResponseTable> result = userSearchService.getUsersTable(requestTable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should return empty when index does not exist")
        void getUsersTable_shouldReturnEmpty_whenIndexNotExists() {
            UserRequestTable requestTable = new UserRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(false);

            PageResponse<UserResponseTable> result = userSearchService.getUsersTable(requestTable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when search fails")
        void getUsersTable_shouldReturnEmpty_whenSearchFails() {
            UserRequestTable requestTable = new UserRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(true);
            when(elasticsearchOperations.search(any(Query.class), eq(UserSearchDocument.class)))
                    .thenThrow(new RuntimeException("Search error"));

            PageResponse<UserResponseTable> result = userSearchService.getUsersTable(requestTable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should search with fullName filter")
        void getUsersTable_withFullNameFilter_shouldSearch() {
            UserRequestTable requestTable = new UserRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            requestTable.setFullName("John");

            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(true);
            when(elasticsearchOperations.search(any(Query.class), eq(UserSearchDocument.class))).thenReturn(searchHits);
            when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);
            when(searchHit.getContent()).thenReturn(testDocument);

            PageResponse<UserResponseTable> result = userSearchService.getUsersTable(requestTable);

            assertThat(result).isNotNull();
            verify(elasticsearchOperations).search(any(Query.class), eq(UserSearchDocument.class));
        }

        @Test
        @DisplayName("Should search with phone filter")
        void getUsersTable_withPhoneFilter_shouldSearch() {
            UserRequestTable requestTable = new UserRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            requestTable.setPhoneNumber("123456");

            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(true);
            when(elasticsearchOperations.search(any(Query.class), eq(UserSearchDocument.class))).thenReturn(searchHits);
            when(searchHits.getSearchHits()).thenReturn(List.of());
            when(searchHits.getTotalHits()).thenReturn(0L);

            PageResponse<UserResponseTable> result = userSearchService.getUsersTable(requestTable);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should search with all filters")
        void getUsersTable_withAllFilters_shouldSearch() {
            UserRequestTable requestTable = new UserRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);
            requestTable.setFullName("John");
            requestTable.setPhoneNumber("123");
            requestTable.setAccountNumber("ACC");
            requestTable.setCityName("City");
            requestTable.setStreetName("Street");
            requestTable.setHouseNumber("123");
            requestTable.setApartmentNumber("1");
            requestTable.setStatus(Status.ACTIVE);

            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(true);
            when(elasticsearchOperations.search(any(Query.class), eq(UserSearchDocument.class))).thenReturn(searchHits);
            when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);
            when(searchHit.getContent()).thenReturn(testDocument);

            PageResponse<UserResponseTable> result = userSearchService.getUsersTable(requestTable);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("indexAllUsers Tests")
    class IndexAllUsersTests {
        @Test
        @DisplayName("Should index all users when index exists")
        void indexAllUsers_shouldReindexWhenIndexExists() {
            City city = new City();
            city.setName("City");
            Street street = new Street();
            street.setName("Street");
            testUser.setCity(city);
            testUser.setStreet(street);

            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(true);
            when(indexOperations.delete()).thenReturn(true);
            when(indexOperations.create()).thenReturn(true);
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            userSearchService.indexAllUsers();

            verify(indexOperations).delete();
            verify(indexOperations).create();
            verify(indexOperations).putMapping();
            verify(userSearchRepository).saveAll(any());
        }

        @Test
        @DisplayName("Should index all users when index does not exist")
        void indexAllUsers_shouldCreateIndexWhenNotExists() {
            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(false);
            when(indexOperations.create()).thenReturn(true);
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            userSearchService.indexAllUsers();

            verify(indexOperations, never()).delete();
            verify(indexOperations).create();
            verify(indexOperations).putMapping();
            verify(userSearchRepository).saveAll(any());
        }

        @Test
        @DisplayName("Should throw exception when indexing fails")
        void indexAllUsers_shouldThrowException_whenFails() {
            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(true);
            when(indexOperations.delete()).thenThrow(new RuntimeException("Delete failed"));

            assertThatThrownBy(() -> userSearchService.indexAllUsers())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Не удалось проиндексировать пользователей");
        }

        @Test
        @DisplayName("Should handle user with null city and street")
        void indexAllUsers_shouldHandleNullCityAndStreet() {
            testUser.setCity(null);
            testUser.setStreet(null);
            testUser.setStatus(null);
            testUser.setRole(null);

            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(false);
            when(indexOperations.create()).thenReturn(true);
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            userSearchService.indexAllUsers();

            verify(userSearchRepository).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Full Name Building Tests")
    class FullNameBuildingTests {
        @Test
        @DisplayName("Should build full name correctly with all parts")
        void shouldBuildFullNameWithAllParts() {
            UserRequestTable requestTable = new UserRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(true);
            when(elasticsearchOperations.search(any(Query.class), eq(UserSearchDocument.class))).thenReturn(searchHits);
            when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);
            when(searchHit.getContent()).thenReturn(testDocument);

            PageResponse<UserResponseTable> result = userSearchService.getUsersTable(requestTable);

            assertThat(result.getContent().get(0).getFullName()).isEqualTo("Doe John Middle");
        }

        @Test
        @DisplayName("Should build full name with missing parts")
        void shouldBuildFullNameWithMissingParts() {
            UserSearchDocument docWithMissingParts = UserSearchDocument.builder()
                    .id("user-id")
                    .lastName("Doe")
                    .build();

            UserRequestTable requestTable = new UserRequestTable();
            requestTable.setPage(0);
            requestTable.setSize(10);

            when(elasticsearchOperations.indexOps(UserSearchDocument.class)).thenReturn(indexOperations);
            when(indexOperations.exists()).thenReturn(true);
            when(elasticsearchOperations.search(any(Query.class), eq(UserSearchDocument.class))).thenReturn(searchHits);
            when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);
            when(searchHit.getContent()).thenReturn(docWithMissingParts);

            PageResponse<UserResponseTable> result = userSearchService.getUsersTable(requestTable);

            assertThat(result.getContent().get(0).getFullName()).isEqualTo("Doe");
        }
    }
}
