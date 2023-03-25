
console.clear();

let win = null;
let saveWin = null;
let count = 1;

let posX = window.innerWidth;
let posY = window.innerHeight;

function setupNewWindow() {

    if (win != null) {
        win.close();
        count ++;
    }

    win = window.open("", "", "toolbar=no, titlebar=no, status=no, menubar=no, top=" + posX + ", left=" + posY + ", width=1, height=1");
    win.addEventListener('load', function() {

        console.clear();
        console.log(count + " puzzle is being cracked...");

        setTimeout(function() {
            getPuzzle(win);
        }, 5000);

    });

    win.location = "https://lichess.org/training/";

}

setupNewWindow();

function getPuzzle(win) {

    let obj = {};

    let game_meta = win.document.getElementsByClassName('infos')[1].getElementsByTagName('span')[0].innerHTML;
    obj.game_type = game_meta.replace(/[\d\+•\s]/g, '');

    try {
        let white = win.document.getElementsByClassName('user-link')[0].innerHTML;
        obj.white_name = white.match(/(^\w*)/)[0];
        obj.white_elo = white.match(/\(([\d]+)\)/)[1];

        let black = win.document.getElementsByClassName('user-link')[1].innerHTML;
        obj.black_name = black.match(/(^\w*)/)[0];
        obj.black_elo = black.match(/\(([\d]+)\)/)[1];
    } catch (err) {
        console.log(err);
        return;
    }

    let gameId = win.document.getElementsByClassName('infos puzzle')[0].getElementsByTagName('a')[0].innerHTML;
    obj.game_key = gameId.substring(1);


    // get the puzzle

    let moves = win.document.getElementsByTagName('move');
    let beforeLen = moves.length;

    let problem = '';
    for(let i = 0; i < beforeLen; i++) {
        problem += moves[i].innerText + ',';
    }
    obj.problem = problem.substring(0, problem.length - 1);

    // prepare the solution
    let solDiv = win.document.getElementsByClassName("view_solution")[0];
    let solA = solDiv.children[0];
    solA.click();

    moves = win.document.getElementsByTagName('move');

    let solution = '';
    for (let i = beforeLen; i < moves.length; i++) {
        solution += moves[i].innerText.replace(/[\r\n|\n|\r]✓/gm, '') + ',';
    }
    obj.solution = solution.substring(0, solution.length-1);

    if (saveWin == null)
        saveWin = win.open("http://localhost/chesspuzz/store_puz.php?json=" + encodeURIComponent(JSON.stringify(obj)), "toolbar=no, titlebar=no, status=no, menubar=no, top=150, left=0, width=720, height=600");
    else saveWin.location = "http://localhost/chesspuzz/store_puz.php?json=" + encodeURIComponent(JSON.stringify(obj));

    setupNewWindow();
}