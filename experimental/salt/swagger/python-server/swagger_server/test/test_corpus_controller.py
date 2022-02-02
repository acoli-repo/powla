# coding: utf-8

from __future__ import absolute_import

from flask import json
from six import BytesIO

from swagger_server.models.response import Response  # noqa: E501
from swagger_server.test import BaseTestCase


class TestCorpusController(BaseTestCase):
    """CorpusController integration test stubs"""

    def test_add_data(self):
        """Test case for add_data

        Send raw data
        """
        data = dict(importer='importer_example',
                    blob='blob_example')
        response = self.client.open(
            '/data/blob/{id}'.format(id='id_example'),
            method='POST',
            data=data,
            content_type='application/x-www-form-urlencoded')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_add_file(self):
        """Test case for add_file

        Upload a corpus file
        """
        data = dict(importer='importer_example',
                    file=(BytesIO(b'some file data'), 'file.txt'))
        response = self.client.open(
            '/data/file/{id}'.format(id='id_example'),
            method='POST',
            data=data,
            content_type='multipart/form-data')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_delete(self):
        """Test case for delete

        Deletes a resource
        """
        response = self.client.open(
            '/data/resource/{id}'.format(id='id_example'),
            method='DELETE')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))

    def test_get_response(self):
        """Test case for get_response

        Get processed data (full or partial)
        """
        response = self.client.open(
            '/data/resource/{id}'.format(id='id_example'),
            method='GET')
        self.assert200(response,
                       'Response body is : ' + response.data.decode('utf-8'))


if __name__ == '__main__':
    import unittest
    unittest.main()
