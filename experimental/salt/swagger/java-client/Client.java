import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.CorpusApi;

import java.io.File;
import java.util.*;

public class Client {

    public static void main(String[] args) {

      CorpusApi apiInstance = new CorpusApi();

      ApiClient ac = apiInstance.getApiClient();
      ac.setConnectTimeout(Integer.MAX_VALUE);
      ac.setReadTimeout(Integer.MAX_VALUE);
      ac.setWriteTimeout(Integer.MAX_VALUE);
      apiInstance.setApiClient(ac);

      String basePath = apiInstance.getApiClient().getBasePath();

      System.err.println("synopsis: Client [-base BasePath] ID PepperImporter FILE[1..n]\n"+
          "\tBasePath       base path, e.g., http://localhost:8080/data/, defaults to "+basePath+"\n"+
          "\tID             string, used for internal resource identification\n"+
          "\tPepperImporter one Pepper importer, e.g., ExmaraldaImporter, PaulaImporter, etc.\n"+
          "\tFILEi          argument file(s), should conform to requirements of the selected PepperImporter");

        String id=null;
        String importer = null;

        for(int i = 0; i<args.length; i++) {
          if(args[i].equalsIgnoreCase("-base")) {
            basePath=args[++i];
            ac.setBasePath(basePath);
            apiInstance.setApiClient(ac);
          } else if(id==null) {
            id=args[i];
          } else if(importer==null) {
            importer=args[i];
          } else { // loop over files
            String file = args[i];
            System.err.println("processing "+file);
            try {
                //apiInstance.addFile(id, importer, new File(file));
                ApiResponse response = apiInstance.addFileWithHttpInfo​(id, importer, new File(file));
                System.out.println("# doc: "+file);
                System.out.println("# response: "+response.getStatusCode());
                System.out.println("# headers: "+response.getHeaders());
//ApiResponse response = client.execute(call, Class.forName("java.lang.String"));
                System.out.println(response.getData());
                // if (response.getData() != null) {
                //   JSonValue errorMessageValue = (JSonValue) response.getData().get("error");
                //   if (errorMessageValue != null) {
                //     errorMessage = (String) errorMessageValue.getValue();
                //   }
                // }

                System.out.println();
            } catch (ApiException e) {
                System.err.println("Exception when calling CorpusApi#addData");
                e.printStackTrace();
            }
          }
        }
    }
}
