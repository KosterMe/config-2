package depviz.repo;

import depviz.Check;
import java.util.List;

/**
 * Тесты разбора файла тестового репозитория.
 */
public final class TestRepositoryTest {

    private TestRepositoryTest() {
    }

    /**
     * Запускает все тесты класса.
     */
    public static void run() {
        definitions();
        separators();
        errors();
        files();
    }

    private static void definitions() {
        TestRepository repo = parse("# comment\nA: B C\n\nB: C\nC:\n");
        Check.equal("test repo: contains A", true, repo.contains("A"));
        Check.equal("test repo: contains C", true, repo.contains("C"));
        Check.equal("test repo: no X", false, repo.contains("X"));
        Check.equal("test repo: deps of A", List.of("B", "C"), repo.dependencies("A"));
        Check.equal("test repo: no deps", List.of(), repo.dependencies("C"));
        Check.equal("test repo: unknown deps", List.of(), repo.dependencies("X"));
    }

    private static void separators() {
        TestRepository repo = parse("  AB :C,  D\r\nC: D\r\n");
        Check.equal("test repo: long name and commas", List.of("C", "D"),
                repo.dependencies("AB"));
        Check.equal("test repo: crlf", List.of("D"), repo.dependencies("C"));
    }

    private static void errors() {
        Check.contains("test repo: lowercase", error("a: B"),
                "line 1: invalid package name 'a'");
        Check.contains("test repo: bad dependency", error("A: B c"),
                "line 1: invalid package name 'c'");
        Check.contains("test repo: digits", error("A: B1"), "invalid package name 'B1'");
        Check.contains("test repo: empty name", error(": B"), "invalid package name ''");
        Check.contains("test repo: no colon", error("A: B\nB C"),
                "line 2: expected 'NAME: DEPENDENCIES', got 'B C'");
        Check.contains("test repo: duplicate", error("A: B\n# x\nA: C"),
                "line 3: package 'A' is already defined at line 1");
        Check.contains("test repo: repeated dependency", error("A: B C B"),
                "dependency 'B' is listed twice");
    }

    private static void files() {
        try {
            TestRepository repo = TestRepository.load("examples/repos/simple.txt");
            Check.equal("test repo: load file", List.of("B", "C"), repo.dependencies("A"));
        } catch (RepositoryException e) {
            Check.isTrue("test repo: load file: " + e.getMessage(), false);
        }
        try {
            TestRepository.load("examples/repos/missing-file.txt");
            Check.isTrue("test repo: missing file", false);
        } catch (RepositoryException e) {
            Check.contains("test repo: missing file", e.getMessage(),
                    "cannot read test repository");
        }
    }

    private static TestRepository parse(String text) {
        try {
            return TestRepository.parse(text);
        } catch (RepositoryException e) {
            throw new AssertionError("unexpected error: " + e.getMessage());
        }
    }

    private static String error(String text) {
        try {
            TestRepository.parse(text);
            return "no error";
        } catch (RepositoryException e) {
            return e.getMessage();
        }
    }
}
