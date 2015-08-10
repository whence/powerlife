var glob = require("glob")
var argv = require('yargs').argv;

glob((argv.path || '**/*.js'), function (er, files) {
  console.log(files);
});
