import com.google.gson.Gson;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;

import org.hyperskill.hstest.testcase.CheckResult;
import task.Main;

import java.util.Random;

import static org.hyperskill.hstest.testcase.CheckResult.correct;
import static org.hyperskill.hstest.testcase.CheckResult.wrong;


public class Tests extends SpringTest {

    public Tests() {
        super(Main.class);
    }

    static void throwIfIncorrectStatusCode(HttpResponse response, int status) {
        if (response.getStatusCode() != status) {
            throw new WrongAnswer(response.getRequest().getMethod() +
                    " " + response.getRequest().getLocalUri() +
                    " should respond with status code " + status +
                    ", responded: " + response.getStatusCode() + "\n\n" +
                    "Response body:\n" + response.getContent());
        }
    }

    static class Task {
        final String name;
        final String description;

        public Task(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    final Task[] TASKS = new Task[]{
            new Task("1", "a"),
            new Task("22", "bb"),
            new Task("abc", "bac"),
            new Task("asl;dfjas;ldfj", "asdfljsdljfksdfl;j"),

            new Task("66666666666666666666666666666666666", "sdfjfjfjfjfjfjfjfjfjfjfjfjfjfjfjfjjsdfjsdf"),
            new Task("666666666666666666666666666666666669", "sdfjfjfjfjfjfjfjfjfjfjfjfjsfjfjfjfjjsdfjsdf"),

            new Task("11111111111111111111111111111111111111111111111111", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
            new Task("2222222222222222222222222222222222222222222222222", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"),
    };

    final Task[] INCORRECT_TASKS = new Task[]{
            new Task(null, null),
            new Task(null, "ab"),
            new Task("ab", null),

            new Task("", ""),
            new Task("", "a"),
            new Task("a", ""),

            new Task(" ", " "),
            new Task("aaaa", " "),
            new Task(" ", "bbbb"),
            new Task("            ", "acvb"),
            new Task("sdf", "       "),

            new Task("111111111111111111111111111111111111111111111111111", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
            new Task("111111111111111111111111111111111111111111111111111", "aaaaaaaaaaaaaaaaaa"),
            new Task("11111111", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
            new Task("2222222222222222222222222222222222222222222222222aaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbaaa"),
            new Task("2222222222222222222222222222222222222222222222222aaa2222222222222222222222222222222222222222222222222aaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbaaa")
    };

    final Gson gson = new Gson();


    @DynamicTest
    final DynamicTesting[] dt = new DynamicTesting[]{
            () -> testPostTask(TASKS[0], 200),
            () -> testPostTask(TASKS[1], 200),
            () -> testPostTask(TASKS[2], 200),
            () -> testPostTask(TASKS[3], 200),
            () -> testPostTask(TASKS[4], 200),
            () -> testPostTask(TASKS[5], 200),
            () -> testPostTask(TASKS[6], 200),
            () -> testPostTask(TASKS[7], 200),

            () -> testPostTask(INCORRECT_TASKS[0], 400),
            () -> testPostTask(INCORRECT_TASKS[1], 400),
            () -> testPostTask(INCORRECT_TASKS[2], 400),
            () -> testPostTask(INCORRECT_TASKS[3], 400),
            () -> testPostTask(INCORRECT_TASKS[4], 400),
            () -> testPostTask(INCORRECT_TASKS[5], 400),
            () -> testPostTask(INCORRECT_TASKS[6], 400),
            () -> testPostTask(INCORRECT_TASKS[7], 400),
            () -> testPostTask(INCORRECT_TASKS[8], 400),
            () -> testPostTask(INCORRECT_TASKS[9], 400),
            () -> testPostTask(INCORRECT_TASKS[11], 400),
            () -> testPostTask(INCORRECT_TASKS[12], 400),
            () -> testPostTask(INCORRECT_TASKS[13], 400),
            () -> testPostTask(INCORRECT_TASKS[14], 400),
            () -> testPostTask(INCORRECT_TASKS[15], 400),
    };

    CheckResult testPostTask(Task task, int status) {
        HttpResponse response = post("/tasks", gson.toJson(task)).send();

        throwIfIncorrectStatusCode(response, status);

        return correct();
    }
}