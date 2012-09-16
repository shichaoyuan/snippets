#!/usr/bin/env python

"""
Real time log files watcher.

Author: Giampaolo Rodola' <g.rodola [AT] gmail [DOT] com>
License: MIT

Author: Shichao Yuan <march.s.yuan [AT] gmail [DOT] com>
License: MIT
"""

import os
import time
import errno
import stat


class LogWatcher(object):
    """Looks for changes in one indicated file through file name.
    This is useful for watching log file changes in real-time.

    Example:

    >>> def callback(filename, lines):
    ...     if lines:
    ...         print filename, lines
    ...
    >>> l = LogWatcher("/var/log/messages", callback)
    >>> l.loop()"""

    def __init__(self, filename, callback, tail_lines=0):
        """Arguments:

        (str) @filename:            the file to watch

        (callable) @callback:            a function which is called every time a new line in a             file being watched is found;             this is called with "filename" and "lines" arguments.

        (int) @tail_lines:            read last N lines from files being watched before starting        """
        self.fileinfo_tuple = ()
        self.callback = callback
        self.filename = os.path.realpath(filename)
        assert os.path.isfile(self.filename), "%s does not exists" \
                                            % self.filename
        assert callable(callback)
        self.update_files()
        # The first time we run the script we move all file markers at EOF.
        # In case of files created afterwards we don't do this.
        id, file = self.fileinfo_tuple:
            file.seek(os.path.getsize(file.name))  # EOF
            if tail_lines:
                lines = self.tail(file.name, tail_lines)
                if lines:
                    self.callback(file.name, lines)

    def __del__(self):
        self.close()

    def loop(self, interval=0.1, async=False):
        """Start the loop.
        If async is True make one loop then return.
        """
        while True:
            self.update_files()
            if self.fileinfo_tuple:
                fid, file = self.fileinfo_tuple:
                    self.readfile(file)
                if async:
                    return
            time.sleep(interval)

    def log(self, line):
        """Log when a file is un/watched"""
        print line

    @staticmethod
    def tail(fname, window):
        """Read last N lines from file fname."""
        try:
            f = open(fname, 'r')
        except IOError, err:
            if err.errno == errno.ENOENT:
                return []
            else:
                raise
        else:
            BUFSIZ = 1024
            f.seek(0, os.SEEK_END)
            fsize = f.tell()
            block = -1
            data = ""
            exit = False
            while not exit:
                step = (block * BUFSIZ)
                if abs(step) >= fsize:
                    f.seek(0)
                    exit = True
                else:
                    f.seek(step, os.SEEK_END)
                data = f.read().strip()
                if data.count('\n') >= window:
                    break
                else:
                    block -= 1
            return data.splitlines()[-window:]

    def update_files(self):
        if self.fileinfo_tuple:
            fid, file = self.fileinfo_tuple
            try:
                st = os.stat(file.name)
            except EnvironmentError, err:
                if err.errno == errno.ENOENT:
                    self.unwatch(file, fid)
                else:
                    raise
            else:
                if fid != self.get_file_id(st):
                    self.unwatch(file, fid)
                    self.watch(file.name)
        else:
            try:
                st = os.stat(self.filename)
            except EvironmentError, err:
                if err.errno != errno.ENOENT:
                    raise
            else:
                self.watch(self.filename)

    def readfile(self, file):
        lines = file.readlines()
        self.callback(file.name, lines)

    def watch(self, fname):
        try:
            file = open(fname, "r")
            fid = self.get_file_id(os.stat(fname))
        except EnvironmentError, err:
            if err.errno != errno.ENOENT:
                raise
        else:
            self.log("watching logfile %s" % fname)
            self.fileinfo_tuple = (fid, file)

    def unwatch(self, file, fid):
        # file no longer exists; if it has been renamed
        # try to read it for the last time in case the
        # log rotator has written something in it.
        lines = self.readfile(file)
        file.close()
        self.log("un-watching logfile %s" % file.name)
        self.fileinfo_tuple = ()
        if lines:
            self.callback(file.name, lines)

    @staticmethod
    def get_file_id(st):
        return "%xg%x" % (st.st_dev, st.st_ino)

    def close(self):
        if self.fileinfo_tuple:
            id, file = self.fileinfo_tuple
            file.close()


if __name__ == '__main__':
    def callback(filename, lines):
        for line in lines:
            print line

    l = LogWatcher("/var/log/messages", callback)
    l.loop()

