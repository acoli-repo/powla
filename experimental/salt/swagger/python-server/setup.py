# coding: utf-8

import sys
from setuptools import setup, find_packages

NAME = "swagger_server"
VERSION = "1.0.0"

# To install the library, run the following
#
# python setup.py install
#
# prerequisite: setuptools
# http://pypi.python.org/pypi/setuptools

REQUIRES = ["connexion"]

setup(
    name=NAME,
    version=VERSION,
    description="PepperReader",
    author_email="christian.chiarcos@gmail.com",
    url="",
    keywords=["Swagger", "PepperReader"],
    install_requires=REQUIRES,
    packages=find_packages(),
    package_data={'': ['swagger/swagger.yaml']},
    include_package_data=True,
    entry_points={
        'console_scripts': ['swagger_server=swagger_server.__main__:main']},
    long_description="""\
    Fintan Docker integration experiment; TODO: update to openapi: 3.0.0, also cf. https://swagger.io/docs/specification/2-0/file-upload/
    """
)

