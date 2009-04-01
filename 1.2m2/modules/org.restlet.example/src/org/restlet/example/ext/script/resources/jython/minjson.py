##############################################################################
#
# Copyright (c) 2006 Zope Corporation and Contributors.
# All Rights Reserved.
#
# This software is subject to the provisions of the Zope Public License,
# Version 2.1 (ZPL).  A copy of the ZPL should accompany this distribution.
# THIS SOFTWARE IS PROVIDED "AS IS" AND ANY AND ALL EXPRESS OR IMPLIED
# WARRANTIES ARE DISCLAIMED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF TITLE, MERCHANTABILITY, AGAINST INFRINGEMENT, AND FITNESS
# FOR A PARTICULAR PURPOSE.
#
##############################################################################

# minjson.py
# reads minimal javascript objects.
# str's objects and fixes the text to write javascript.

#UNICODE USAGE:  Minjson tries hard to accommodate naive usage in a 
#"Do what I mean" manner.  Real applications should handle unicode separately.
# The "right" way to use minjson in an application is to provide minjson a 
# python unicode string for reading and accept a unicode output from minjson's
# writing.  That way, the assumptions for unicode are yours and not minjson's.

# That said, the minjson code has some (optional) unicode handling that you 
# may look at as a model for the unicode handling your application may need.

# Thanks to Patrick Logan for starting the json-py project and making so many
# good test cases.

# Additional thanks to Balazs Ree for replacing the writing module.

# Jim Washington 6 Dec 2006.

# 2006-12-06 Thanks to Koen van de Sande, now handles the case where someone 
#            might want e.g., a literal "\n" in text not a new-line.
# 2005-12-30 writing now traverses the object tree instead of relying on 
#            str() or unicode()
# 2005-10-10 on reading, looks for \\uxxxx and replaces with u'\uxxxx'
# 2005-10-09 now tries hard to make all strings unicode when reading.
# 2005-10-07 got rid of eval() completely, makes object as found by the
#            tokenizer.
# 2005-09-06 imported parsing constants from tokenize; they changed a bit from
#            python2.3 to 2.4
# 2005-08-22 replaced the read sanity code
# 2005-08-21 Search for exploits on eval() yielded more default bad operators.
# 2005-08-18 Added optional code from Koen van de Sande to escape
#            outgoing unicode chars above 128


from re import compile, sub, search, DOTALL
from token import ENDMARKER, NAME, NUMBER, STRING, OP, ERRORTOKEN
from tokenize import tokenize, TokenError, NL

#Usually, utf-8 will work, set this to utf-16 if you dare.
emergencyEncoding = 'utf-8'

class ReadException(Exception):
    pass

class WriteException(Exception):
    pass

#################################
#      read JSON object         #
#################################

slashstarcomment = compile(r'/\*.*?\*/',DOTALL)
doubleslashcomment = compile(r'//.*\n')

unichrRE = compile(r"\\u[0-9a-fA-F]{4,4}")

def unichrReplace(match):
    return unichr(int(match.group()[2:],16))

escapeStrs = (('\n',r'\n'),('\b',r'\b'),
    ('\f',r'\f'),('\t',r'\t'),('\r',r'\r'), ('"',r'\"')
    )

class DictToken:
    __slots__=[]
    pass
class ListToken:
    __slots__=[]
    pass
class ColonToken:
    __slots__=[]
    pass
class CommaToken:
    __slots__=[]
    pass

class JSONReader(object):
    """raise SyntaxError if it is not JSON, and make the object available"""
    def __init__(self,data):
        self.stop = False
        #make an iterator of data so that next() works in tokenize.
        self._data = iter([data])
        self.lastOp = None
        self.objects = []
        self.tokenize()

    def tokenize(self):
        try:
            tokenize(self._data.next,self.readTokens)
        except TokenError:
            raise SyntaxError

    def resolveList(self):
        #check for empty list
        if isinstance(self.objects[-1],ListToken):
            self.objects[-1] = []
            return
        theList = []
        commaCount = 0
        try:
            item = self.objects.pop()
        except IndexError:
            raise SyntaxError
        while not isinstance(item,ListToken):
            if isinstance(item,CommaToken):
                commaCount += 1
            else:
                theList.append(item)
            try:
                item = self.objects.pop()
            except IndexError:
                raise SyntaxError
        if not commaCount == (len(theList) -1):
            raise SyntaxError
        theList.reverse()
        item = theList
        self.objects.append(item)

    def resolveDict(self):
        theList = []
        #check for empty dict
        if isinstance(self.objects[-1], DictToken):
            self.objects[-1] = {}
            return
        #not empty; must have at least three values
        try:
            #value (we're going backwards!)
            value = self.objects.pop()
        except IndexError:
            raise SyntaxError
        try:
            #colon
            colon = self.objects.pop()
            if not isinstance(colon, ColonToken):
                raise SyntaxError
        except IndexError:
            raise SyntaxError
        try:
            #key
            key = self.objects.pop()
            if not isinstance(key,basestring):
                raise SyntaxError
        except IndexError:

            raise SyntaxError
        #salt the while
        comma = value
        while not isinstance(comma,DictToken):
            # store the value
            theList.append((key,value))
            #do it again...
            try:
                #might be a comma
                comma = self.objects.pop()
            except IndexError:
                raise SyntaxError
            if isinstance(comma,CommaToken):
                #if it's a comma, get the values
                try:
                    value = self.objects.pop()
                except IndexError:
                    #print self.objects
                    raise SyntaxError
                try:
                    colon = self.objects.pop()
                    if not isinstance(colon, ColonToken):
                        raise SyntaxError
                except IndexError:
                    raise SyntaxError
                try:
                    key = self.objects.pop()
                    if not isinstance(key,basestring):
                        raise SyntaxError
                except IndexError:
                    raise SyntaxError
        theDict = {}
        for k in theList:
            theDict[k[0]] = k[1]
        self.objects.append(theDict)

    def readTokens(self,type, token, (srow, scol), (erow, ecol), line):
        # UPPERCASE consts from tokens.py or tokenize.py
        if type == OP:
            if token not in "[{}],:-":
                raise SyntaxError
            else:
                self.lastOp = token
            if token == '[':
                self.objects.append(ListToken())
            elif token == '{':
                self.objects.append(DictToken())
            elif token == ']':
                self.resolveList()
            elif token == '}':
                self.resolveDict()
            elif token == ':':
                self.objects.append(ColonToken())
            elif token == ',':
                self.objects.append(CommaToken())
        elif type == STRING:
            tok = token[1:-1]
            parts = tok.split("\\\\")
            for k in escapeStrs:
                if k[1] in tok:
                    parts = [part.replace(k[1],k[0]) for part in parts]
            self.objects.append("\\".join(parts))
        elif type == NUMBER:
            if self.lastOp == '-':
                factor = -1
            else:
                factor = 1
            try:
                self.objects.append(factor * int(token))
            except ValueError:
                self.objects.append(factor * float(token))
        elif type == NAME:
            try:
                self.objects.append({'true':True,
                    'false':False,'null':None}[token])
            except KeyError:
                raise SyntaxError
        elif type == ENDMARKER:
            pass
        elif type == NL:
            pass
        elif type == ERRORTOKEN:
            if ecol == len(line):
                #it's a char at the end of the line.  (mostly) harmless.
                pass
            else:
                raise SyntaxError
        else:
            raise SyntaxError
    def output(self):
        try:
            assert len(self.objects) == 1
        except AssertionError:
            raise SyntaxError
        return self.objects[0]

def safeRead(aString, encoding=None):
    """read the js, first sanitizing a bit and removing any c-style comments
    If the input is a unicode string, great.  That's preferred.  If the input 
    is a byte string, strings in the object will be produced as unicode anyway.
    """
    # get rid of trailing null. Konqueror appends this.
    CHR0 = chr(0)
    while aString.endswith(CHR0):
        aString = aString[:-1]
    # strip leading and trailing whitespace
    aString = aString.strip()
    # zap /* ... */ comments
    aString = slashstarcomment.sub('',aString)
    # zap // comments
    aString = doubleslashcomment.sub('',aString)
    # detect and handle \\u unicode characters. Note: This has the side effect
    # of converting the entire string to unicode. This is probably OK.
    unicodechars = unichrRE.search(aString)
    if unicodechars:
        aString = unichrRE.sub(unichrReplace, aString)
    #if it's already unicode, we won't try to decode it
    if isinstance(aString, unicode):
        s = aString
    else:
        if encoding:
            # note: no "try" here.  the encoding provided must work for the
            # incoming byte string.  UnicodeDecode error will be raised
            # in that case.  Often, it will be best not to provide the encoding
            # and allow the default
            s = unicode(aString, encoding)
            #print "decoded %s from %s" % (s,encoding)
        else:
            # let's try to decode to unicode in system default encoding
            try:
                s = unicode(aString)
                #import sys
                #print "decoded %s from %s" % (s,sys.getdefaultencoding())
            except UnicodeDecodeError:
                # last choice: handle as emergencyEncoding
                enc = emergencyEncoding
                s = unicode(aString, enc)
                #print "%s decoded from %s" % (s, enc)
    # parse and get the object.
    try:
        data = JSONReader(s).output()
    except SyntaxError:
        raise ReadException, 'Unacceptable JSON expression: %s' % aString
    return data

read = safeRead

#################################
#   write object as JSON        #
#################################

import re, codecs
from cStringIO import StringIO

### Codec error handler

def jsonreplace_handler(exc):
    '''Error handler for json

    If encoding fails, \\uxxxx must be emitted. This
    is similar to the "backshashreplace" handler, only
    that we never emit \\xnn since this is not legal
    according to the JSON syntax specs.
    '''
    if isinstance(exc, UnicodeEncodeError):
        part = exc.object[exc.start]
        # repr(part) will convert u'\unnnn' to u'u\\nnnn'
        return u'\\u%04x' % ord(part), exc.start+1
    else:
        raise exc

# register the error handler
#Jython dosn't support this
#codecs.register_error('jsonreplace', jsonreplace_handler)

### Writer

def write(input, encoding='utf-8', outputEncoding=None):
    writer = JsonWriter(input_encoding=encoding, output_encoding=outputEncoding)
    writer.write(input)
    return writer.getvalue()

re_strmangle = re.compile('"|\b|\f|\n|\r|\t|\\\\')

def func_strmangle(match):
    return {
        '"': '\\"',
        '\b': '\\b',
        '\f': '\\f',
        '\n': '\\n',
        '\r': '\\r',
        '\t': '\\t',
        '\\': '\\\\',
        }[match.group(0)]

def strmangle(text):
    return re_strmangle.sub(func_strmangle, text)

class JsonStream(object):

    def __init__(self):
        self.buf = []

    def write(self, text):
        self.buf.append(text)

    def getvalue(self):
        return ''.join(self.buf)

class JsonWriter(object):

    def __init__(self, stream=None, input_encoding='utf-8', output_encoding=None):
        '''
        - stream is optional, if specified must also give output_encoding
        - The input strings can be unicode or in input_encoding
        - output_encoding is optional, if omitted, result will be unicode
        '''
        if stream is not None:
            if output_encoding is None:
                raise WriteException, 'If a stream is given, output encoding must also be provided'
        else:
            stream = JsonStream()
        self.stream = stream
        self.input_encoding = input_encoding
        self.output_encoding = output_encoding

    def write(self, obj):
        if isinstance(obj, (list, tuple)):
            self.stream.write('[')
            first = True
            for elem in obj:
                if first:
                    first = False
                else:
                    self.stream.write(',')
                self.write(elem)
            self.stream.write(']'),
        elif isinstance(obj, dict):
            self.stream.write('{')
            first = True
            for key, value in obj.iteritems():
                if first:
                    first = False
                else:
                    self.stream.write(',')
                self.write(key)
                self.stream.write(':')
                self.write(value)
            self.stream.write('}')
        elif obj is True:
            self.stream.write('true')
        elif obj is False:
            self.stream.write('false')
        elif obj is None:
            self.stream.write('null')
        elif not isinstance(obj, basestring):
            # if we are not baseobj, convert to it
            try:
                obj = str(obj)
            except Exception, exc:
                raise WriteException, 'Cannot write object (%s: %s)' % (exc.__class__, exc)
            self.stream.write(obj)
        else:
            # convert to unicode first
            if not isinstance(obj, unicode):
                try:
                    obj = unicode(obj, self.input_encoding)
                except (UnicodeDecodeError, UnicodeTranslateError):
                    obj = unicode(obj, 'utf-8', 'replace')
            # do the mangling
            obj = strmangle(obj)
            # make the encoding
            if self.output_encoding is not None:
                obj = obj.encode(self.output_encoding, 'jsonreplace')
            self.stream.write('"')
            self.stream.write(obj)
            self.stream.write('"')

    def getvalue(self):
        return self.stream.getvalue()


