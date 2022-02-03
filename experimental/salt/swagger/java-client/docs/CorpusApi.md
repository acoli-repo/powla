# CorpusApi

All URIs are relative to *https://to.be.determin.ed/data*

Method | HTTP request | Description
------------- | ------------- | -------------
[**addData**](CorpusApi.md#addData) | **POST** /blob/{id} | Send raw data
[**addFile**](CorpusApi.md#addFile) | **POST** /file/{id} | Upload a corpus file
[**delete**](CorpusApi.md#delete) | **DELETE** /resource/{id} | Deletes a resource
[**getResponse**](CorpusApi.md#getResponse) | **GET** /resource/{id} | Get processed data (full or partial)


<a name="addData"></a>
# **addData**
> Response addData(id, importer, format, blob)

Send raw data

resource/data ID

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.CorpusApi;


CorpusApi apiInstance = new CorpusApi();
String id = "id_example"; // String | resource/data ID
String importer = "importer_example"; // String | PepperImporter
String format = "format_example"; // String | target format, one of CoNLL-RDF, CoNLL or POWLA
String blob = "blob_example"; // String | Data to be processed
try {
    Response result = apiInstance.addData(id, importer, format, blob);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CorpusApi#addData");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**| resource/data ID |
 **importer** | **String**| PepperImporter |
 **format** | **String**| target format, one of CoNLL-RDF, CoNLL or POWLA |
 **blob** | **String**| Data to be processed |

### Return type

[**Response**](Response.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/x-www-form-urlencoded
 - **Accept**: Not defined

<a name="addFile"></a>
# **addFile**
> Response addFile(id, importer, format, file)

Upload a corpus file

path to a local file

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.CorpusApi;


CorpusApi apiInstance = new CorpusApi();
String id = "id_example"; // String | resource ID
String importer = "importer_example"; // String | PepperImporter
String format = "format_example"; // String | target format, one of CoNLL-RDF, CoNLL or POWLA
File file = new File("/path/to/file.txt"); // File | File to upload
try {
    Response result = apiInstance.addFile(id, importer, format, file);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CorpusApi#addFile");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**| resource ID |
 **importer** | **String**| PepperImporter |
 **format** | **String**| target format, one of CoNLL-RDF, CoNLL or POWLA |
 **file** | **File**| File to upload |

### Return type

[**Response**](Response.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: application/xml, application/json

<a name="delete"></a>
# **delete**
> delete(id)

Deletes a resource



### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.CorpusApi;


CorpusApi apiInstance = new CorpusApi();
String id = "id_example"; // String | identifier as used for POST request
try {
    apiInstance.delete(id);
} catch (ApiException e) {
    System.err.println("Exception when calling CorpusApi#delete");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**| identifier as used for POST request |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/xml, application/json

<a name="getResponse"></a>
# **getResponse**
> Response getResponse(id)

Get processed data (full or partial)

result (full or partial)

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.CorpusApi;


CorpusApi apiInstance = new CorpusApi();
String id = "id_example"; // String | identifier as used for POST request
try {
    Response result = apiInstance.getResponse(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CorpusApi#getResponse");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **String**| identifier as used for POST request |

### Return type

[**Response**](Response.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/xml, application/json

