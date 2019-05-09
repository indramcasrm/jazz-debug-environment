package org.jazzcommunity.development;

import java.io.IOException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.jazzcommunity.development.library.FileTools;
import org.jazzcommunity.development.library.VersionChecker;
import org.jazzcommunity.development.library.zip.Zip;

public class SetupTask extends DefaultTask {

  private String sdk;

  /**
   * This will have to read the version from the command line, otherwise, the newest version will be
   * used. If no files are available, this needs to print a warning.
   */
  @TaskAction
  public void initialize() throws IOException {
    // so if the sdk version is empty, we find the newest one...
    // I'm not yet sure how that is going to work with non-major releases, but we'll see
    String version = sdk.isEmpty() ? FileTools.newestVersion("jde/sdks") : sdk;
    System.out.println(String.format("SDK Version %s selected for runtime setup", version));

    // abort if a runtime already exists
    if (FileTools.exists(String.format("jde/runtime/%s", version))) {
      System.out.println(String.format("Runtime for %s already exists. Doing nothing.", version));
      return;
    }

    // VersionChecker is just a sanity test for what can be set up.
    // 1) check sdk
    // 2) check server
    // 3) check database
    // 4) check sdk_files.cfg
    if (VersionChecker.canSetup(version)) {
      setup(version);
    }
  }

  private static void setup(String version) throws IOException {
    // 1) extract sdk
    Zip.extract(
        FileTools.byVersion("jde/sdks", version),
        FileTools.toAbsolute(String.format("jde/runtime/%s/sdk", version)));
    // 2) extract server
    Zip.extract(
        FileTools.byVersion("jde/servers", version),
        FileTools.toAbsolute(String.format("jde/runtime/%s/jre", version)),
        "server/jre");
    // 3) extract database
    Zip.extract(
        FileTools.byVersion("jde/dbs", version),
        FileTools.toAbsolute(String.format("jde/runtime/%s/db", version)));
    // 4) copy other necessary static files, probably also needs checks...
    // I'm still hoping that I can eventually run this stuff without needing the run files...
    copyConfigs(version);
    // 5) set executable bit on necessary files
    FileTools.setExecutable(
        FileTools.toAbsolute(String.format("jde/runtime/%s/jre/bin/java", version)));
  }

  private static void copyConfigs(String version) {
    String source = String.format("tool/configs/%s", version);
    String target = String.format("jde/runtime/%s/conf", version);

    System.out.println(String.format("Copying configuration files from %s to %s", source, target));

    FileTools.makeDirectory(FileTools.toAbsolute(target));
    FileTools.copyAll(source, target);
  }

  @Option(option = "sdk", description = "Which SDK version to set up. Default is latest.")
  public void setSdk(String sdk) {
    this.sdk = sdk;
  }
}
