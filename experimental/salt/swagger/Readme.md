# OpenAPI-compliant container for Pepper/SALT-based conversion

(HTTP codes from https://en.wikipedia.org/wiki/List_of_HTTP_status_codes#1xx_informational_response)

REST ops:
POST (not PUT) one resource
possible responses:
- 202 (ok)
- 406 (wrong input)
- 409 (conflict: not empty)
- 408 (timeout)
- ?413 (payload too large: hitting a processing limit)

must accept files (for loaders) or plain content (for updates)

GET one response, until empty (<=> stdout)
if multiple resources are to be processed, PUT the next one, then
- 202 (accepted: processing not concluded yet, no data)
- 206 (partial content, if there is more to read, data)
- 200 (ok, i.e., no next element, data)
- 208 (already reported, for GET requests after last element, no data)

=> read chunk by chunk like from stdin
  note: didn't figure out how to control repsonse codes, yet, so we only do PUSH and read off the result en bloc

Open API supports file upload
- cf. https://swagger.io/docs/specification/describing-request-body/file-upload/

- generation using https://swagger.io/swagger-codegen/, edit using https://editor.swagger.io/?_ga=2.228928156.1002408929.1643562911-25616704.1643562911


server and client code generated using swaggerhub.com

run with

    pip3 install -r requirements.txt
    python3 -m swagger_server

Note: You may need to update connexion in `requirements.txt` to 2.6.0 or higher: https://github.com/zalando/connexion/issues/1149

access:

  browser: http://localhost:8080/data/ui

  curl -X 'POST' 'http://localhost:8080/data/blob/sample_id' -F 'blob=this is a test' -F 'importer=TextImporter'

for file upload, add @ before path:

  curl -X 'POST' 'http://localhost:8080/data/file/myid1' -F 'file=@README.md' -F 'importer=TextImporter'


build Docker container

  docker build -t acoli:toRDF .

starting container

  docker run -p 8080:8080 acoli:toRDF

UI:
  http://172.17.0.2:8080/data/ui/#/corpus


todo:
- make id optional, if missing, we will generate a new new id (non-existent target directory)
- clearall command: remove all local files
- list ids
