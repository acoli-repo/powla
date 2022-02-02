import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.CorpusApi;

import java.io.File;
import java.util.*;

public class Client {

    public static void main(String[] args) {

        System.err.println("synopsis: Client ID PepperImporter FILE[1..n]\n"+
          "\tID             string, used for internal resource identification\n"+
          "\tPepperImporter one Pepper importer, e.g., ExmaraldaImporter, PaulaImporter, etc.\n"+
          "\tFILEi          argument file(s), should conform to requirements of the selected PepperImporter");

        CorpusApi apiInstance = new CorpusApi();
        String id = args[0]; // String | resource/data ID
        String importer = args[1]; // String | PepperImporter
        for(int i=2; i<args.length; i++) {
          String file = args[i];
          System.err.println("processing "+file);
          try {
              System.out.println("# "+file);
              apiInstance.addFile(id, importer, new File(file));
              System.out.println();
          } catch (ApiException e) {
              System.err.println("Exception when calling CorpusApi#addData");
              e.printStackTrace();
          }
        }
    }
}
