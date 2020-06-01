package es.ewald.intellij.bulkrename;

import com.opencsv.exceptions.CsvException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * Tests for {@link RenameTask}.
 */
public class RenameTaskTest {

  @Test
  public void testLoadTasks() throws IOException, CsvException {
    String csvFile = getClass().getClassLoader().getResource("sample-refactoring.csv").getFile();
    List<RenameTask> renameTasks = RenameTask.loadTasks(csvFile);
    assertThat("Tasks are correctly created",
        renameTasks,
        contains(new RenameTask("/absolute/path/to/project/src/main/java/some/package/Sample.java",
                "Sample2",
                true,
                true),
            new RenameTask("/absolute/path/to/project/src/main/java/some/package/AnotherSample.java",
                "AnotherSample2",
                false,
                true),
            new RenameTask("/absolute/path/to/project/src/main/java/some/package/YetAnotherSample.java",
                "YetAnotherSample2",
                true,
                false)));
  }

}