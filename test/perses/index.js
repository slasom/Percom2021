const fs = require('fs');
const readline = require('readline');
const path = require('path');
const yaml = require('js-yaml');


////////////// LOAD PERSES CONFIGURATION FILE ///////////////////

var rootPath=path.normalize(".");;

var configFileName = rootPath+"/perses-config.yml";
var config = {};


//Load first device log file

var resultFileName = rootPath+"/devices-logs/output-log-android1.txt";

console.log("Loading Perses Config ("+configFileName+").");
try {
    config = yaml.safeLoad(fs.readFileSync(configFileName, 'utf8'));

} catch (e) {
  console.error(e);
  process.exit(1);
}



//////////////

const fileStream = fs.createReadStream(resultFileName);

 readInterface = readline.createInterface({
    input: fileStream,
    console: false
});

var values=0
var items=0
var badLines = 0;
var totalLines = 0;

//Heatmap-Log
readInterface.on('line', function(line) {
    totalLines++;
    lineSplit = line.split(",");
    var value = Number(lineSplit[lineSplit.length-1]);
    if(Number.isNaN(value)){
        badLines++;
        //console.log("Skiping wrong log line: <"+line+"> -> splited: ["+lineSplit+"], value: ["+value+"]");
    }else{
      console.log("Good log line: <"+line+"> -> splited: ["+lineSplit+"], value: ["+value+"] from a total of "+items);
      values += value;
      items++;
    }
});

readInterface.on('close', function(){
    avg = Number((values/items).toFixed(3));
    console.log("Total Lines: "+totalLines+" Bad log lines: "+badLines+" Good log lines: "+items);

    if(Number.isNaN(avg)){
      console.error("TEST FAILED: result_device1_avg is NaN");
      process.exit(1);
    } else if (avg > config.max_avg_devices){
        console.error("TEST FAILED: result_device1_avg > max_avg_devices --> "+avg+ " > "+config.max_avg_devices);
        process.exit(1);
    }else{
        console.log("TEST PASSED: result_device1 < max_avg_devices --> "+avg+ " > "+config.max_avg_devices);
    }
});
