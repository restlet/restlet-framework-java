#!/usr/bin/env jython

# $Id: test_zxjdbc_dbapi20.py 2281 2003-04-10 20:17:43Z bzimmer $

"""
This is to be used with the DB API compatibility test available @
  http://stuartbishop.net/Software/DBAPI20TestSuite/
"""

__rcs_id__  = '$Id: test_zxjdbc_dbapi20.py 2281 2003-04-10 20:17:43Z bzimmer $'
__version__ = '$Revision: 2281 $'

import dbapi20
import unittest
from com.ziclix.python.sql import zxJDBC as zxjdbc

class test_zxjdbc(dbapi20.DatabaseAPI20Test):
    driver = zxjdbc
    connect_args = ("jdbc:postgresql://localhost/ziclix", "bzimmer", "", "org.postgresql.Driver")
    connect_kw_args = {}
    
    def test_nextset(self): pass
    def test_setoutputsize(self): pass

if __name__ == '__main__':
    unittest.main()
