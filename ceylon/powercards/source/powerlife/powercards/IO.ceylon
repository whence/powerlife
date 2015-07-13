shared interface IO {
    shared formal String input();
    shared formal void output(String message);
}

shared object consoleIO satisfies IO {
    shared actual String input() => process.readLine() else "";
    shared actual void output(String message) {
        print(message);
        print('\n');
    }
}
