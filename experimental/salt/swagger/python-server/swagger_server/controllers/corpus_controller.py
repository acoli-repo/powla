import connexion
import six
import subprocess
import tempfile
import os

from swagger_server.models.response import Response  # noqa: E501
from swagger_server import util

# can we get something like a global variable?
id2tmpfile={}

def add_data(id, importer, blob):  # noqa: E501
    """Send raw data

    resource/data ID # noqa: E501

    :param id: resource/data ID
    :type id: str
    :param importer: PepperImporter
    :type importer: str
    :param format: target format, one of CoNLL-RDF, CoNLL or POWLA
    :type format: str
    :param blob: Data to be processed
    :type blob: str

    :rtype: Response
    """
    return Response(value=blob) # still unprocessed ... # TODO: write to file and then call add_file


def add_file(id, importer, file):  # noqa: E501
    """Upload a corpus file

    path to a local file # noqa: E501

    :param id: resource ID
    :type id: str
    :param importer: PepperImporter
    :type importer: str
    :param format: target format, one of CoNLL-RDF, CoNLL or POWLA
    :type format: str
    :param file: File to upload
    :type file: werkzeug.datastructures.FileStorage

    :rtype: Response
    """

    tmpfile="/tmp/salt/"+id+"/"+file.filename
    tmpdir=os.path.abspath(os.path.join(tmpfile, os.pardir))
    if not os.path.exists(tmpdir):
        os.makedirs(tmpdir)
    if not os.path.exists(tmpfile):
            id2tmpfile[id]=tmpfile
            file.save(tmpfile)
    #         return str(id2tmpfile)
    #
    # return "it's me: "+"Popen "+file.name
    proc = subprocess.Popen(["toRDF",importer,tmpfile], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    # proc = subprocess.Popen(["ls","-l",tmpfile], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout, stderr = proc.communicate()
    # return "when calling toRDF "+importer+" "+str(tmpfile)+":\n"+str(stdout)+"\nERR: "+"\nERR: ".join(str(stderr).split("\n"))
    # return str(stdout)

    result=stdout.decode("utf-8").strip()
    if len(result)==0:
        result="ERROR LOG:\n"+stderr.decode("utf-8").strip()

    return Response(format="POWLA-RDF", value=result)

    # content=file.stream.readlines()
    # content=[ line.decode("utf-8",errors="ignore") for line in content ]
    # content="".join(content)
    #
    # proc = subprocess.Popen(["echo", content], stdout = subprocess.PIPE,
    #                                            stderr = subprocess.PIPE)
    # stdout, stderr = proc.communicate()
    #
    # return str(stdout)

#    return add_data(id,content)
    # with open(file,"rt",errors="ignore") as input:
    #     return add_data(id,input.reads())


def delete(id):  # noqa: E501
    """Deletes a resource

     # noqa: E501

    :param id: identifier as used for POST request
    :type id: str

    :rtype: None
    """

    return "it's me: "+id+" vs. "+str(id2tmpfile)
    # return id2tmpfile[id]
    # if id in id2tmpfile:
    #     tmpfile=id2tmpfile[id]
    #     id2tmpfile.remove(id)
    #     if os.path.exists(tmpfile):
    #         os.delete(tmpfile)
    #         return "ok"

def get_response(id):  # noqa: E501
    """Get processed data (full or partial)

    result (full or partial) # noqa: E501

    :param id: identifier as used for POST request
    :type id: str

    :rtype: Response
    """
    return 'right now, we just return everything as the response of POST. TODO: return object by object via multiple GET requests with different response statuses'
