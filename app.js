var elections = [];
for(var i = 2012; i >= 1956; i -= 4) {
    elections.push(require(`./rawdata/election_data_${i}.json`));
}

function sanitizeName(s) {
    return String(s).replace(/[^A-Za-z\s]/g, "")
}

function sanitizeNumber(s) {
    var r = String(s).replace(/[^0123456789\.]/g, "");
    return (r === "")? "0" : r;
}

var id = 0;
for(var i = 0; i < elections.length; i++) {
    var year = elections[i].electionYear
    for(var y = 0; y < elections[i].data.length; y++) {
        var t = elections[i].data[y];
        var state = sanitizeName(t.state);
        var totalVotes = sanitizeNumber(t.totalVotes);
        var voteDemocrat = sanitizeNumber(t.voteDemocrat);
        var voteRepublican = sanitizeNumber(t.voteRepublican);
        var perVoteDemocrat = sanitizeNumber(t.perVoteDemocrat);
        var perVoteRepublican = sanitizeNumber(t.perVoteRepublican);
        var evDemocrat = sanitizeNumber(t.electVoteDemocrat)
        var evRepublican = sanitizeNumber(t.electVoteRepublican);
        console.log(
            `${id},${year},${state},${totalVotes},${voteDemocrat},${voteRepublican},${perVoteDemocrat},${perVoteRepublican},${evDemocrat},${evRepublican}`
        );
        id++;
    }
}