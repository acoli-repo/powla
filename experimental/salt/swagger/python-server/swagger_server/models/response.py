# coding: utf-8

from __future__ import absolute_import
from datetime import date, datetime  # noqa: F401

from typing import List, Dict  # noqa: F401

from swagger_server.models.base_model_ import Model
from swagger_server import util


class Response(Model):
    """NOTE: This class is auto generated by the swagger code generator program.

    Do not edit the class manually.
    """

    def __init__(self, format: str=None, value: str=None):  # noqa: E501
        """Response - a model defined in Swagger

        :param format: The format of this Response.  # noqa: E501
        :type format: str
        :param value: The value of this Response.  # noqa: E501
        :type value: str
        """
        self.swagger_types = {
            'format': str,
            'value': str
        }

        self.attribute_map = {
            'format': 'format',
            'value': 'value'
        }

        self._format = format
        self._value = value

    @classmethod
    def from_dict(cls, dikt) -> 'Response':
        """Returns the dict as a model

        :param dikt: A dict.
        :type: dict
        :return: The Response of this Response.  # noqa: E501
        :rtype: Response
        """
        return util.deserialize_model(dikt, cls)

    @property
    def format(self) -> str:
        """Gets the format of this Response.

        response format  # noqa: E501

        :return: The format of this Response.
        :rtype: str
        """
        return self._format

    @format.setter
    def format(self, format: str):
        """Sets the format of this Response.

        response format  # noqa: E501

        :param format: The format of this Response.
        :type format: str
        """
        allowed_values = ["POWLA-RDF", "CoNLL-RDF", "CoNLL-TSV"]  # noqa: E501
        if format not in allowed_values:
            raise ValueError(
                "Invalid value for `format` ({0}), must be one of {1}"
                .format(format, allowed_values)
            )

        self._format = format

    @property
    def value(self) -> str:
        """Gets the value of this Response.


        :return: The value of this Response.
        :rtype: str
        """
        return self._value

    @value.setter
    def value(self, value: str):
        """Sets the value of this Response.


        :param value: The value of this Response.
        :type value: str
        """
        if value is None:
            raise ValueError("Invalid value for `value`, must not be `None`")  # noqa: E501

        self._value = value
