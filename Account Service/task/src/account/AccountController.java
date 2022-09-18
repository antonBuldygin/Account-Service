package account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Validated
public class AccountController {

    List<String> breachedPasswords = new ArrayList<>(Arrays.asList("PasswordForJanuary", "PasswordForFebruary",
            "PasswordForMarch", "PasswordForApril",
            "PasswordForMay", "PasswordForJune",
            "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober",
            "PasswordForNovember", "PasswordForDecember"));

    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    PaymentsRepository paymentsRepository;
    @Autowired
    LogRepository logRepository;

    @Autowired
    private UserDetailsServiceImpl userService;

//    @Autowired
//    DataLoader dataLoader;

    @Autowired
    GroupRepository groupRepository;

    List<Payments> paymentsList = new ArrayList<>();

    @PutMapping(value = "api/admin/user/access", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Secured("ROLE_ADMINISTRATOR")
    public ResponseEntity<?> lockUnlockUser(@AuthenticationPrincipal UserDetails details, @RequestBody LockUnlock lockUnlock) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");
        String date = LocalDateTime.now().format(dateTimeFormatter);
        Optional<User> emailInRepo = userRepository.findByEmail(lockUnlock.getUser().toLowerCase()).stream().findFirst();
        User user = null;
        Map<String, String> map = new HashMap<>();
        Set<Group> list = emailInRepo.get().getRolesToStore();
        List<Group> adminPresent = list.stream().filter(x -> x.getRole().equals("ROLE_ADMINISTRATOR")).collect(Collectors.toList());
        if (!adminPresent.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        }
        if (emailInRepo.isPresent()) {
            user = emailInRepo.get();

        }
        new UserDetailsImpl(user);
        if (user != null && lockUnlock.getOperation().equals("UNLOCK")) {
            if (!user.isAccountNonLocked()) {
                user.setAccountNonLocked(true);
                user.setFailedAttempt(0);
                userRepository.save(user);


                logRepository.save(new Log(date, "UNLOCK_USER", details.getUsername(), "Unlock user " + user.getEmail(), "/api/admin/user/access"));
                map = Map.of("status", "User " + user.getEmail() + " unlocked!");
            }
        }

        if (user != null && lockUnlock.getOperation().equals("LOCK")) {
            if (user.isAccountNonLocked()) {
                user.setAccountNonLocked(false);
                userRepository.save(user);
                logRepository.save(new Log(date, "LOCK_USER", details.getUsername(), "Lock user " + user.getEmail(), "/api/admin/user/access"));
                map = Map.of("status", "User " + user.getEmail() + " locked!");
                return new ResponseEntity<>(map, HttpStatus.OK);

            }
        }
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @GetMapping("api/security/events")
    @Secured({"ROLE_AUDITOR"})
    public ResponseEntity<?> logForAudit(@AuthenticationPrincipal UserDetails details) {
        Iterable<Log> logOptional = logRepository.findAll();

        return new ResponseEntity<>(logOptional, HttpStatus.OK);
    }

    @PutMapping("api/admin/user/role")
    @Secured({"ROLE_ADMINISTRATOR"})
    public ResponseEntity<?> rolesUpdate(@AuthenticationPrincipal UserDetails details,
                                         @RequestBody(required = false) RoleUpdate roleUpdate) {

        if (roleUpdate.getRole().equals(null)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found!");
        }
        Optional<User> emailInRepo = userRepository.findByEmail(roleUpdate.getUser().toLowerCase()).stream().findFirst();
        User userToReturn = null;

        if (!emailInRepo.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!");
        }
        if (emailInRepo.isPresent()) {
            userToReturn = emailInRepo.get();
            Set<Group> list = emailInRepo.get().getRolesToStore();

            List<Group> adminPresent = list.stream().filter(x -> x.getRole().equals("ROLE_ADMINISTRATOR")).collect(Collectors.toList());
            List<Group> accountantPresent = list.stream().filter(x -> x.getRole().equals("ROLE_ACCOUNTANT")).collect(Collectors.toList());
            List<Group> userPresent = list.stream().filter(x -> x.getRole().equals("ROLE_USER")).collect(Collectors.toList());
            List<Group> auditorPresent = list.stream().filter(x -> x.getRole().equals("ROLE_AUDITOR")).collect(Collectors.toList());

            if (roleUpdate.getOperation().equals("REMOVE")
            ) {
                int count = 0;
                for (Group gr : list
                ) {
                    if (gr.getRole().equals("ROLE_" + roleUpdate.getRole())) {
                        count++;
                    }

                }
                if (count == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
                }
            }

            if (!roleUpdate.getRole().equals("ADMINISTRATOR") && !roleUpdate.getRole().equals("ACCOUNTANT")
                    && !roleUpdate.getRole().equals("USER") && !roleUpdate.getRole().equals("AUDITOR")) {

                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
            }

            if (roleUpdate.getRole().equals("ADMINISTRATOR") && roleUpdate.getOperation().equals("REMOVE")) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
            }
            if (roleUpdate.getRole().equals("ADMINISTRATOR")) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The user cannot combine administrative and business roles!");
            }
            if (!adminPresent.isEmpty()
            ) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user cannot combine administrative and business roles!");
            }
            if (roleUpdate.getOperation().equals("REMOVE") && list.size() == 1
            ) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
            }
//**********************************************
//            if (adminPresent.isEmpty() && userPresent.isEmpty() && accountantPresent.isEmpty()
//
//                    && roleUpdate.getOperation().equals("GRANT")
//                    && roleUpdate.getRole().equals("ACCOUNTANT")) {
//
//                userToReturn.addRole(new Group("ROLE_ACCOUNTANT"));
//                userRepository.saveAndFlush(userToReturn);
//                userToReturn.setRoles();
//
//                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");
//
//                String date = LocalDateTime.now().format(dateTimeFormatter);
//                logRepository.save(new Log(date, "GRANT_ROLE", details.getUsername(),
//                        "Grant role ACCOUNTANT to " + userToReturn.getEmail(), "/api/admin/user/role"));
//
//                return new ResponseEntity<>(userToReturn, HttpStatus.OK);
//            }
//
//            if (accountantPresent.isEmpty() && userPresent.isEmpty()
//
//                    && roleUpdate.getOperation().equals("GRANT")
//                    && roleUpdate.getRole().equals("USER")) {
//
//                userToReturn.addRole(new Group("ROLE_USER"));
//                userRepository.saveAndFlush(userToReturn);
//                userToReturn.setRoles();
//
//                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");
//
//                String date = LocalDateTime.now().format(dateTimeFormatter);
//                logRepository.save(new Log(date, "GRANT_ROLE", details.getUsername(),
//                        "Grant role USER to " + userToReturn.getEmail(), "/api/admin/user/role"));
//
//                return new ResponseEntity<>(userToReturn, HttpStatus.OK);
//            }
//*********************************************
            if (accountantPresent.isEmpty() && roleUpdate.getOperation().equals("GRANT")
                    && roleUpdate.getRole().equals("ACCOUNTANT")) {

                userToReturn.addRole(new Group("ROLE_ACCOUNTANT"));
                userRepository.saveAndFlush(userToReturn);
                userToReturn.setRoles();

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

                String date = LocalDateTime.now().format(dateTimeFormatter);
                logRepository.save(new Log(date, "GRANT_ROLE", details.getUsername(),
                        "Grant role ACCOUNTANT to " + userToReturn.getEmail(), "/api/admin/user/role"));

                return new ResponseEntity<>(userToReturn, HttpStatus.OK);
            }

            if (userPresent.isEmpty() && roleUpdate.getOperation().equals("GRANT")
                    && roleUpdate.getRole().equals("USER")) {
                userToReturn.addRole(new Group("ROLE_USER"));
                userRepository.saveAndFlush(userToReturn);
                userToReturn.setRoles();

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

                String date = LocalDateTime.now().format(dateTimeFormatter);
                logRepository.save(new Log(date, "GRANT_ROLE", details.getUsername(),
                        "Grant role USER to " + userToReturn.getEmail(), "/api/admin/user/role"));
                return new ResponseEntity<>(userToReturn, HttpStatus.OK);

            }

            if (auditorPresent.isEmpty() && roleUpdate.getOperation().equals("GRANT")
                    && roleUpdate.getRole().equals("AUDITOR")) {
                userToReturn.addRole(new Group("ROLE_AUDITOR"));
                userRepository.saveAndFlush(userToReturn);
                userToReturn.setRoles();

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

                String date = LocalDateTime.now().format(dateTimeFormatter);
                logRepository.save(new Log(date, "GRANT_ROLE", details.getUsername(),
                        "Grant role AUDITOR to " + userToReturn.getEmail(), "/api/admin/user/role"));
                return new ResponseEntity<>(userToReturn, HttpStatus.OK);

            }

            if (!userPresent.isEmpty()
                    && roleUpdate.getRole().equals("USER") && roleUpdate.getOperation().equals("REMOVE")
            ) {

                userToReturn.removeRole(userPresent.get(0));
                userRepository.saveAndFlush(userToReturn);
                userToReturn.setRoles();

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

                String date = LocalDateTime.now().format(dateTimeFormatter);
                logRepository.save(new Log(date, "REMOVE_ROLE", details.getUsername(),
                        "Remove role USER from " + userToReturn.getEmail(), "/api/admin/user/role"));
                return new ResponseEntity<>(userToReturn, HttpStatus.OK);

            }

            if (!accountantPresent.isEmpty()
                    && roleUpdate.getRole().equals("ACCOUNTANT") && roleUpdate.getOperation().equals("REMOVE")
            ) {

                userToReturn.removeRole(accountantPresent.get(0));
                userRepository.saveAndFlush(userToReturn);
                userToReturn.setRoles();

                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

                String date = LocalDateTime.now().format(dateTimeFormatter);
                logRepository.save(new Log(date, "REMOVE_ROLE", details.getUsername(),
                        "Remove role ACCOUNTANT from " + userToReturn.getEmail(), "/api/admin/user/role"));
                return new ResponseEntity<>(userToReturn, HttpStatus.OK);

            }

            if (!auditorPresent.isEmpty()
                    && roleUpdate.getRole().equals("AUDITOR") && roleUpdate.getOperation().equals("REMOVE")
            ) {

                userToReturn.removeRole(accountantPresent.get(0));
                userRepository.saveAndFlush(userToReturn);
                userToReturn.setRoles();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

                String date = LocalDateTime.now().format(dateTimeFormatter);
                logRepository.save(new Log(date, "REMOVE_ROLE", details.getUsername(),
                        "Remove role AUDITOR from " + userToReturn.getEmail(), "/api/admin/user/role"));

                return new ResponseEntity<>(userToReturn, HttpStatus.OK);

            }
        }

        userRepository.saveAndFlush(userToReturn);
        userToReturn.setRoles();
        return new ResponseEntity<>(userToReturn, HttpStatus.OK);
    }

    @DeleteMapping("api/admin/user")
    @Secured({"ROLE_ADMINISTRATOR"})
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails details) {

        return new ResponseEntity<>("e-mail?", HttpStatus.OK);
    }

    @DeleteMapping("api/admin/user/{email}")
    @Secured({"ROLE_ADMINISTRATOR"})
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetails details,
                                        @PathVariable @Pattern(regexp = "[a-zA-Z0-9._%+-]+@acme.com") String email) {
        if (userRepository.findByEmail(email.toLowerCase()).isPresent()) {
            Set<Group> list = userRepository
                    .findByEmail(email.toLowerCase()).get().getRolesToStore();

            for (Group gr : list
            ) {
                if (gr.getRole().contains("ROLE_ADMINISTRATOR")) {

                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
                }
            }

        }

        List<User> user = userRepository.deleteByEmail(email.toLowerCase());
        if (user.size() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trying to delete non existing user!");
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

        String date = LocalDateTime.now().format(dateTimeFormatter);
        logRepository.save(new Log(date, "DELETE_USER", details.getUsername(),
                user.get(0).getEmail(), "/api/admin/user"));

        user.forEach(users -> users.setRoles());

        Map<String, String> map = Map.of("user", user.get(0).getEmail(), "status", "Deleted successfully!");

        return new ResponseEntity<>(map, HttpStatus.OK);
    }


    @GetMapping("api/admin/user")
    @Secured({"ROLE_ADMINISTRATOR"})
    public ResponseEntity<?> allUsersInformation(@AuthenticationPrincipal UserDetails details) {
        Iterable<User> userRepositoryAll = userRepository.findAll();
        List<User> target = new ArrayList<>();
        userRepositoryAll.forEach(target::add);
        target.forEach(user -> user.setRoles());

        return new ResponseEntity<>(target, HttpStatus.OK);
    }

    @PostMapping(value = "api/acct/payments", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Transactional
    @Secured("ROLE_ACCOUNTANT")
    public ResponseEntity<?> createPayments(@RequestBody(required = false) Payments[] payments) {

//        paymentsRepository.deleteAll();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy");

        for (Payments item : payments
        ) {
            try {
                YearMonth date = YearMonth.parse(item.getPeriod(), dateTimeFormatter);
                System.out.println(date);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "incorrect date");
            }


            Optional<User> emailInRepo = userRepository.findByEmail(item.getEmployee().toLowerCase()).stream().findFirst();
            String employee = item.getEmployee().toLowerCase();
            Optional<Payments> paymentsOptional = paymentsRepository.findByEmployeeAndAndPeriod(employee, item.getPeriod()).stream().findFirst();


            if (paymentsOptional.isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "dublicated salary period for  " + paymentsOptional.get().getEmployee() + " " + paymentsOptional.get().getPeriod());
            }
            if (!emailInRepo.isPresent() || item.getSalary() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Error!");
            }

            if (emailInRepo.isPresent() && item.getSalary() >= 0 && !paymentsOptional.isPresent()) {
                item.setEmployee(item.getEmployee().toLowerCase());
                paymentsRepository.save(item);
                Iterable<Payments> paymentsIterable = paymentsRepository.findAll();
                paymentsIterable.forEach(System.out::println);
            }

        }


        Map<String, String> map = Map.of("status", "Added successfully!");
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PutMapping(value = "api/acct/payments", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Secured("ROLE_ACCOUNTANT")
    public ResponseEntity<?> updatePayments(@RequestBody Payments payment) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy");


        try {
            YearMonth date = YearMonth.parse(payment.getPeriod(), dateTimeFormatter);
            System.out.println(date);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "incorrect date");
        }


        Optional<User> emailInRepo = userRepository.findByEmail(payment.getEmployee().toLowerCase()).stream().findFirst();
        String employee = payment.getEmployee().toLowerCase();
        Optional<Payments> paymentsOptional = paymentsRepository.findByEmployeeAndAndPeriod(employee, payment.getPeriod()).stream().findFirst();


//            if (paymentsOptional.isPresent()) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                        "dublicated salary period for  " +paymentsOptional.get().getEmployee()+" "+paymentsOptional.get().getPeriod());
//            }
        if (!emailInRepo.isPresent() || payment.getSalary() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error!");
        }
        if (!paymentsOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Period or e-mail not correct!");
        }
        if (emailInRepo.isPresent() && payment.getSalary() >= 0 && paymentsOptional.isPresent()) {
//            payment.setEmployee(payment.getEmployee().toLowerCase());
            Long id = paymentsOptional.get().getId();
            Payments paymentsToUpdate = paymentsRepository.findById(id).get();
            paymentsToUpdate.setSalary(payment.getSalary());
            paymentsRepository.saveAndFlush(paymentsToUpdate);
            Iterable<Payments> paymentsIterable = paymentsRepository.findAll();
            paymentsIterable.forEach(System.out::println);
        }


        Map<String, String> map = Map.of("status", "Updated successfully!");
        return new ResponseEntity<>(map, HttpStatus.OK);
    }


    @PostMapping("api/auth/changepass")
    @Secured({"ROLE_ACCOUNTANT", "ROLE_USER", "ROLE_ADMINISTRATOR"})
    public ResponseEntity<?> changePass(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Newpassword
            new_password) {

        try {
            new_password.getNew_password().equals("");
        } catch (NullPointerException e) {

            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (new_password.getNew_password().equals("")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (new_password.getNew_password().length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        }
        for (String item : breachedPasswords
        ) {
            if (item.equals(new_password.getNew_password())) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The password is in the hacker's database!");
            }

        }


        if (encoder.matches(new_password.getNew_password(), userDetails.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        }

        long id;
        Optional<User> user = userRepository.findByEmail(userDetails.getUsername()).stream().findFirst();
        if (user.isPresent()) {
            id = user.get().getId();
        } else {
            throw new UsernameNotFoundException(String.format("Username [%s] not found", userDetails.getUsername()));
        }


        Optional<User> userById = userRepository.findById(id);
        if (userById.isPresent()) {
            User u = userById.get();

            u.setPassword(encoder.encode(new_password.getNew_password()));

//        user.setEmail(user.getEmail().toLowerCase());

            userRepository.saveAndFlush(u);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

            String date = LocalDateTime.now().format(dateTimeFormatter);
            logRepository.save(new Log(date, "CHANGE_PASSWORD", u.getEmail(), u.getEmail(), "/api/auth/changepass"));

        }


        Map<String, Object> body = new LinkedHashMap<>();

        body.put("email", userDetails.getUsername());
        body.put("status", "The password has been updated successfully");

        return new ResponseEntity<>(body, HttpStatus.OK);

    }


    @PostMapping("api/auth/signup")
    public ResponseEntity<?> signUp(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody User user) {
        try {
            user.getEmail().equals("");
            user.getName().equals("");
            user.getLastname().equals("");
            user.getPassword().equals("");
        } catch (NullPointerException e) {

            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "param is null");
        }
        if (user.getEmail().equals("") || user.getName().equals("") || user.getLastname().equals("") ||
                user.getPassword().equals("")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "param is empty");
        }

        if (!user.getEmail().contains("@acme.com")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (user.getPassword().length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password length must be at least 12!");
        }
        for (String item : breachedPasswords
        ) {
            if (item.equals(user.getPassword())) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The password is in the hacker's database!");
            }

        }

//        System.out.println(userRepository.findAll());
        Iterable<User> userList = userRepository.findAll();

        for (User userw : userList
        ) {
            System.out.println(userw.getEmail() + " " + userw.getName());
        }

//        System.out.println("found"+  userRepository.findById(29L).get().getEmail());

//        userRepository.deleteAll();

        Optional<User> email = userRepository.findByEmail(user.getEmail().toLowerCase()).stream().findFirst();
        if (email.isPresent()) {
            if ((user.getEmail()).equalsIgnoreCase(email.get().getEmail().toLowerCase())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
            }
        }

        Iterable<User> userRepositoryAll = userRepository.findAll();
        List<User> target = new ArrayList<>();
        userRepositoryAll.forEach(target::add);
        if (target.size() > 0) {
            user.setPassword(encoder.encode(user.getPassword()));
            user.addRole(new Group("ROLE_USER"));
            user.setEmail(user.getEmail().toLowerCase());
        } else {
            user.setPassword(encoder.encode(user.getPassword()));
            user.setEmail(user.getEmail().toLowerCase());
            user.addRole(new Group("ROLE_ADMINISTRATOR"));
        }

        user.setRoles();
        userRepository.save(user);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss");

        String date = LocalDateTime.now().format(dateTimeFormatter);
        logRepository.save(new Log(date, "CREATE_USER", "Anonymous", user.getEmail(), "/api/auth/signup"));
        return new ResponseEntity<>(user, HttpStatus.OK);

    }

    @GetMapping("api/empl/payment")
    @Secured({"ROLE_ACCOUNTANT", "ROLE_USER"})
    public ResponseEntity<?> testAuthentication(@AuthenticationPrincipal UserDetails details,
                                                @RequestParam(required = false, name = "period") String period) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy");

        if (period != null) {
            try {
                YearMonth date = YearMonth.parse(period, dateTimeFormatter);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "incorrect date");
            }
            Optional<Payments> paymentsOptional = paymentsRepository.findByEmployeeAndAndPeriod(details.getUsername(), period);
            if (paymentsOptional.isPresent()) {
                Payments payments = paymentsOptional.get();
                System.out.println(details.getUsername());
                User userName = userRepository.findByEmail(details.getUsername()).get();
                int usd = payments.getSalary() / 100;
                int cents = payments.getSalary() - usd * 100;
                String salary = String.format("%d dollar(s) %d cent(s)", usd, cents);

                dateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy");
                YearMonth date = YearMonth.parse(payments.getPeriod(), dateTimeFormatter);
                dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM-yyyy", Locale.ENGLISH);

                String data = dateTimeFormatter.format(date);
                PaymentsToPresent paymentsToPresent = new PaymentsToPresent(salary, userName.getName(), userName.getLastname(), data);
                return new ResponseEntity<>(paymentsToPresent, HttpStatus.OK);
            }
        }
        List<Payments> paymentsOptional = paymentsRepository.findByEmployee(details.getUsername());

        List<PaymentsToPresent> paymentsToPresentList = new ArrayList<>();
        for (Payments payment : paymentsOptional
        ) {
            User userName = userRepository.findByEmail(details.getUsername()).get();
            int usd = payment.getSalary() / 100;
            int cents = payment.getSalary() - usd * 100;
            String salary = String.format("%d dollar(s) %d cent(s)", usd, cents);

            dateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy");
            YearMonth date = YearMonth.parse(payment.getPeriod(), dateTimeFormatter);
            dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM-yyyy", Locale.ENGLISH);

            String data = dateTimeFormatter.format(date);
            PaymentsToPresent paymentsToPresent = new PaymentsToPresent(salary, userName.getName(), userName.getLastname(), data);
            paymentsToPresentList.add(paymentsToPresent);
        }
        Collections.reverse(paymentsToPresentList);
        return new ResponseEntity<>(paymentsToPresentList, HttpStatus.OK);
    }
}


//for (Payments item : payments
//        ) {
//        YearMonth date = YearMonth.parse(item.getPeriod(), dateTimeFormatter);
//
//        System.out.println(date);
//        Optional<User> emailInRepo = userRepository.findByEmail(item.getEmployee().toLowerCase()).stream().findFirst();
//        if (!paymentsList.isEmpty()) {
//
//        Map<String, String> map = paymentsList.stream().collect(Collectors.toMap(
//        p -> p.getPeriod(),
//        p -> p.getEmployee()));
//
//        Optional<Map.Entry<String, String>> filteredbyPeriod = map.entrySet().stream().filter(x -> x.getValue().equals(item.getEmployee()) &&
//        x.getKey().equals(item.getPeriod())).findFirst();
//
//
//        if (emailInRepo.isPresent() && item.getSalary() >= 0 && !filteredbyPeriod.isPresent()) {
//        paymentsList.add(item);
//        map.clear();
//        }
//        } else if (emailInRepo.isPresent() && item.getSalary() >= 0) {
//        paymentsList.add(item);
//        }
//        }
