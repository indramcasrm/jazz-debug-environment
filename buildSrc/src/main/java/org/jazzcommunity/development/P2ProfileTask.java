package org.jazzcommunity.development;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.jazzcommunity.development.library.FileTools;
import org.jazzcommunity.development.maven.SettingsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2ProfileTask extends DefaultTask {

  private String sdk;
  private String out;
  private boolean create;

  public P2ProfileTask() {}

  @TaskAction
  public void useP2Profile() throws JAXBException, IOException, XMLStreamException {
    File m2 = new File(String.format("%s/.m2/settings.xml", System.getProperty("user.home")));

    if (create && m2.exists()) {
      Logger logger = LoggerFactory.getLogger(this.getClass().getName());
      String message =
          String.format(
              "A maven settings file already exists in %s. Use the --sdk option to manage active profiles.",
              System.getProperty("user.home"));
      logger.error(message);
      return;
    }

    if (create && !m2.exists()) {
      m2.createNewFile();
      new SettingsBuilder()
          .addProfiles()
          .addAllRepositories()
          .setActive(FileTools.newestVersion("jde/p2repo"))
          .marshal(getOutput(m2));

      return;
    }

    if (sdk != null) {
      FileTools.backupFile(m2);
      new SettingsBuilder(m2)
          .clearProfiles()
          .addAllRepositories()
          .setActive(sdk.isEmpty() ? FileTools.newestVersion("jde/p2repo") : sdk)
          .marshal(getOutput(m2));

      return;
    }
  }

  private OutputStream getOutput(File m2) throws FileNotFoundException {
    return out == null || !out.equals("file") ? System.out : new FileOutputStream(m2);
  }

  @Option(
      option = "sdk",
      description = "Which sdk version to use for maven builds. Default is latest.")
  public void setSdk(String sdk) {
    this.sdk = sdk;
  }

  @Option(option = "out", description = "Where to output maven profile XML. Default is 'file'.")
  public void setOut(String out) {
    this.out = out;
  }

  @Option(
      option = "create",
      description = "Maven configuration is created if undetected. Default is false")
  public void setCreate(String create) {
    this.create = Boolean.valueOf(create);
  }
}
