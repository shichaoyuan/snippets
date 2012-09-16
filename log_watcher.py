#!/usr/bin/env python

"""
Log files watcher.

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
    This is useful for watching log file changes.
    """

    def __init__(self, filename):
        """Arguments:

        (str) @filename:
            the file to watch

        (int) @tail_lines:
            read last N lines from files being watched before starting
        """
        self.fileinfo_tuple = ()
        self.filename = os.path.realpath(filename)
        assert os.path.isfile(self.filename), "%s does not exists" \
                                            % self.filename
        assert callable(callback)
        self.update_files()
        # The first time we run the script we move all file markers at EOF.
        # In case of files created afterwards we don't do this.
        id, file = self.fileinfo_tuple:
        file.seek(os.path.getsize(file.name))  # EOF

    def __del__(self):
        self.close()

    def loop(self):
        """Start one loop."""
        self.update_files()
        if self.fileinfo_tuple:
            fid, file = self.fileinfo_tuple:
            return self.readfile(file)
        return None

    def log(self, line):
        """Log when a file is un/watched"""
        print line

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
        return lines

    def watch(self, fname):
        try:
            file = open(fname, "r")
            fid = self.get_file_id(os.stat(fname))
        except EnvironmentError, err:
            if err.errno != errno.ENOENT:
                raise
        else:
            self.fileinfo_tuple = (fid, file)
            self.log("watching logfile %s" % fname)

    def unwatch(self, file, fid):
        # file no longer exists
        file.close()
        self.fileinfo_tuple = ()
        self.log("un-watching logfile %s" % file.name)

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

