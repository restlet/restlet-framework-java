#!/usr/bin/env node

/**
 * FORKED FROM ANGULAR.JS
 *
 * Git COMMIT-MSG hook for validating commit message
 * See https://docs.google.com/document/d/1rk04jEuGfk9kYzfqCuOlPTSJw3hEDZJTBN5E5f1SALo/edit
 *
 * Installation:
 * >> cd <repo>
 * >> ln -s ../../resources/git/hooks/validate-commit-msg.js .git/hooks/commit-msg
 */

'use strict';

var fs = require('fs');
var util = require('util');


var MAX_LENGTH = 150;
var PATTERN = /^(?:fixup!\s*)?(\w*)(\(([\w\$\.\*/-]*)\))?\: (.+) (\((https\:\/\/restlet\.atlassian\.net\/browse\/)?AS\-\d+\))$/;
var CHORE_PATTERN = /^(?:fixup!\s*)?(\w*)(\(([\w\$\.\*/-]*)\))?\: (.+)$/;
var MERGE_PATTERN = /^Merge tag .+? into .+$/;
var IGNORED = /^WIP\:/;
var TYPES = {
  feat: true,
  fix: true,
  docs: true,
  style: true,
  refactor: true,
  perf: true,
  test: true,
  chore: true,
  revert: true
};

var beer = '\uD83C\uDF7A  ';
var jack = '\uD83C\uDF83  ';
var tadaa = '\uD83C\uDF89  ';
var girl_desk = '\uD83D\uDC81  ';
var dancer = '\uD83D\uDC83  ';

var error = function () {
  // gitx does not display it
  // http://gitx.lighthouseapp.com/projects/17830/tickets/294-feature-display-hook-error-message-when-hook-fails
  // https://groups.google.com/group/gitx/browse_thread/thread/a03bcab60844b812
  console.log('');
  console.log(tadaa + '¯\\_(ツ)_/¯' + tadaa);
  console.error(jack + 'INVALID COMMIT MSG: ' + util.format.apply(null, arguments));
  console.log('');
};


var validateMessage = function (message) {
  
  if (MERGE_PATTERN.test(message)) {
    return true;
  }

  if (IGNORED.test(message)) {

    var persona = Math.floor(Math.random() * 10) % 2 === 0 ? girl_desk : dancer;
    console.log('(⌐▨_▨) ' + persona + 'Commit message validation ignored.');
    return true;
  }

  if (message.length > MAX_LENGTH) {
    error('is longer than %d characters !', MAX_LENGTH);
    return false;
  }

  var match;

  if (/^chore/.test(message)) { //
    match = CHORE_PATTERN.exec(message);

  } else {
    match = PATTERN.exec(message);
  }


  if (!match) {
    var msg = [
      'does not match "<type>(<scope>): <subject> (AS-<issue_number>)" ! was: ' + message,
      '<type> can be: feat, fix, docs, style, refactor, perf, test, chore or revert',
      '<scope> arbitrary string (ex: databrowser, entitystore, sqlwrapper, api, connector)'
    ]

    error(msg.join('\n'));

    return false;
  }

  var type = match[ 1 ];
  var scope = match[ 3 ];
  var subject = match[ 4 ];
  var issueNb = match[ 5 ];

  if (!TYPES.hasOwnProperty(type)) {
    error('"%s" is not allowed type !', type);
    return false;
  }

  // Some more ideas, do want anything like this ?
  // - allow only specific scopes (eg. fix(docs) should not be allowed ?
  // - auto correct the type to lower case ?
  // - auto correct first letter of the subject to lower case ?
  // - auto add empty line after subject ?
  // - auto remove empty () ?
  // - auto correct typos in type ?
  // - store incorrect messages, so that we can learn

  console.info('(∩^‿^)⊃━☆ﾟ.*･｡ﾟ  ' + beer + 'Commit message is valid');
  return true;
};


var firstLineFromBuffer = function (buffer) {
  var lines = buffer.toString().split('\n');

  var messages = lines.filter(function (line) {
    return /^[^#]/.test(line); // remove comments
  });

  return messages.shift();
};

// publish for testing
exports.validateMessage = validateMessage;

// hacky start if not run by jasmine :-D
if (process.argv.join('').indexOf('jasmine-node') === -1) {

  var commitMsgFile = process.argv[ 2 ];
  var incorrectLogFile = commitMsgFile.replace('COMMIT_EDITMSG', 'logs/incorrect-commit-msgs');

  fs.readFile(commitMsgFile, function (err, buffer) {
    var msg = firstLineFromBuffer(buffer);

    if (!validateMessage(msg)) {

      fs.appendFile(incorrectLogFile, msg + '\n', function () {
        process.exit(1);
      });

    } else {
      process.exit(0);
    }
  });
}
