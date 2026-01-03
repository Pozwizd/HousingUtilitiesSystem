package org.spacelab.housingutilitiessystemadmin.controller.users;

//@Controller
//@RequestMapping("/users")
//@AllArgsConstructor
//@Slf4j
public class UserController {
//
//    private final UserService userService;
//
//    @Autowired
//    @Qualifier("securityExecutor")  // ← Используйте новое имя
//    private Executor executor;
//
//    @PostMapping("/getAll")
//    @ResponseBody
//    public ResponseEntity<Page<UserResponseTable>> getUserResponseTable(@Valid @RequestBody UserRequestTable userRequestTable) {
//        return ResponseEntity.ok(userService.getUsersTable(userRequestTable));
//    }
//
//    @GetMapping("/getUser/{id}")
//    @ResponseBody
//    public CompletableFuture<ResponseEntity<UserResponse>> getUser(@PathVariable ObjectId id) {
//
//        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
//        log.info("[{}] 📥 CONTROLLER: Получен запрос на /users/getUser/{}",
//                Thread.currentThread().getName(), id);
//        log.info("SecurityContext в начале контроллера: {}",
//                SecurityContextHolder.getContext().getAuthentication() != null ?
//                        SecurityContextHolder.getContext().getAuthentication().getName() : "null");
//
//        return CompletableFuture.supplyAsync(
//                SecurityContextUtils.wrapWithSecurityContext(() -> {
//
//                    log.info("[{}] 🔄 ASYNC поток начал работу", Thread.currentThread().getName());
//                    log.info("[{}] SecurityContext в async: {}",
//                            Thread.currentThread().getName(),
//                            SecurityContextHolder.getContext().getAuthentication().getName());
//
//                    UserResponse response = userService.getUserById(id);
//
//                    log.info("[{}] ✅ Данные получены: {}",
//                            Thread.currentThread().getName(), response.getEmail());
//
//                    ResponseEntity<UserResponse> responseEntity = ResponseEntity.ok(response);
//
//                    log.info("[{}] 📦 ResponseEntity создан, status: {}",
//                            Thread.currentThread().getName(), responseEntity.getStatusCode());
//                    log.info("[{}] 📦 Body не null: {}",
//                            Thread.currentThread().getName(), responseEntity.getBody() != null);
//
//                    return responseEntity;
//
//                }),
//                executor
//        ).whenComplete((result, throwable) -> {
//            if (throwable != null) {
//                log.error("[{}] ❌ ОШИБКА в CompletableFuture: ",
//                        Thread.currentThread().getName(), throwable);
//            } else {
//                log.info("[{}] ✅ CompletableFuture завершен успешно",
//                        Thread.currentThread().getName());
//                log.info("[{}] Status code: {}",
//                        Thread.currentThread().getName(), result.getStatusCode());
//            }
//            log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
//        });
//    }
//
//    @PostMapping("/create")
//    @ResponseBody
//    public ResponseEntity<UserResponse> createUser(@Valid @ModelAttribute UserRequest userRequest) {
//        return ResponseEntity.ok(userService.createUser(userRequest));
//    }
//
//    @PutMapping("/{id}")
//    @ResponseBody
//    public ResponseEntity<UserResponse> updateUser(@PathVariable ObjectId id, @Valid @ModelAttribute UserRequest userRequest) {
//        return ResponseEntity.ok(userService.updateUser(id, userRequest));
//    }
//
//    @DeleteMapping("/{id}")
//    @ResponseBody
//    public ResponseEntity<Boolean> deleteUser(@PathVariable ObjectId id) {
//        return ResponseEntity.ok(userService.deleteUser(id));
//    }
//
//    @GetMapping("/getStatuses")
//    @ResponseBody
//    public ResponseEntity<List<Status>> getStatuses() {
//        return ResponseEntity.ok(List.of(Status.values()));
//    }
//
//    @GetMapping({"/", ""})
//    public ModelAndView getHorizontalPage(Model model) {
//        return new ModelAndView("user/users").addObject("pageActive", "users");
//    }
//
//    @GetMapping("/create")
//    public ModelAndView getUserCreatePage(Model model) {
//        model.addAttribute("pageTitle", "users.createUser");
//        model.addAttribute("pageActive", "users");
//        model.addAttribute("isEdit", false);
//        model.addAttribute("opened", true);
//        return new ModelAndView("user/user-edit");
//    }
//
//    @GetMapping("/edit/{id}")
//    public ModelAndView getUserEditPage(@PathVariable ObjectId id, Model model) {
//        model.addAttribute("pageTitle", "users.editUser");
//        model.addAttribute("pageActive", "users");
//        model.addAttribute("isEdit", true);
//        model.addAttribute("opened", true);
//        return new ModelAndView("user/user-edit");
//    }
//
//    @GetMapping("/card/{id}")
//    public ModelAndView showUserProfile(@PathVariable ObjectId id, Model model) {
//        model.addAttribute("pageTitle", "users.user");
//        model.addAttribute("pageActive", "users");
//        model.addAttribute("opened", true);
//        return new ModelAndView("user/userCard");
//    }

}