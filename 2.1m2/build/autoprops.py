#!/usr/bin/env python

# Copyright (C) 2005 Matthew Good <svn@matt-good.net>
#
# "THE BEER-WARE LICENSE" (Revision 42):
# <svn@matt-good.net> wrote this file.  As long as you retain this notice you
# can do whatever you want with this stuff.  If we meet some day, and you think
# this stuff is worth it, you can buy me a beer in return.  Matthew Good
# (Beer-ware license written by Poul-Henning Kamp
#  http://people.freebsd.org/~phk/)
#
# Author: Matthew Good <svn@matt-good.net>

from __future__ import generators

CMD = 'svn'
#CMD = 'svk'
CFG = ['~/.subversion/config',
       '$APPDATA/Subversion/config']


import ConfigParser
import os.path
import fnmatch
import popen2

def versionedfiles(dir):
    out, _ = popen2.popen2('%s status -v %s' % (CMD, dir.replace(' ', '\\ ')))
    items = [line[3:].split(None, 3)[-1][:-1] for line in out
             if not line.startswith('?')]
    return [i for i in items if os.path.isfile(i)]

def autoprops():
    cfg = ConfigParser.ConfigParser()
    from os.path import expanduser, expandvars
    cfg.read([expandvars(expanduser(path)) for path in CFG])

    def props(cfg, pattern):
        for p in cfg.get('auto-props', pattern).split(';'):
            yield (p.split('=', 1) + ['*'])[:2]

    for pattern in cfg.options('auto-props'):
        yield pattern, props(cfg, pattern)

def setprop(prop, value, files):
    cmd = [CMD, 'propset', prop, value] + files
    st = os.spawnvp(os.P_WAIT, CMD, cmd)
    if st:
        print 'Command "%s" failed with exit status %s' \
              % (' '.join(cmd), st)
        sys.exit(1)

def applyprops(dir):
    files = versionedfiles(dir)
    for pattern, props in autoprops():
        matches = fnmatch.filter(files, pattern)
        if matches:
            for prop, value in props:
                setprop(prop, value, matches)

if __name__ == '__main__':
    import sys
    applyprops('.')
