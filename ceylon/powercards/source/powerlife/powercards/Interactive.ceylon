import powerlife.powercards.choices {
    One,
    NonSelectable,
    Skip,
    Multiple,
	nonSelectable
}

shared interface Interactive {
    shared formal One|NonSelectable chooseOne(String message, [Item+] items);
    shared formal One|NonSelectable|Skip chooseSkippableOne(String message, [Item+] items);
    shared formal Multiple|NonSelectable|Skip chooseSkippableMulti(String message, [Item+] items);
}

shared interface ConsoleInteractive satisfies Interactive {
	String input() => process.readLine() else "";
	
	shared actual One|NonSelectable chooseOne(String message, [Item+] items) {
		return nonSelectable;
	}
	shared actual One|NonSelectable|Skip chooseSkippableOne(String message, [Item+] items) {
		return nonSelectable;
	}
	shared actual Multiple|NonSelectable|Skip chooseSkippableMulti(String message, [Item+] items) {
		return nonSelectable;
	}
}

shared interface Connection {
	shared formal void send(String message);
	shared formal String receive();
}

shared interface NetworkInteractive satisfies Interactive {
	shared formal Connection connection;
	
	shared actual One|NonSelectable chooseOne(String message, [Item+] items) {
		return nonSelectable;
	}
	shared actual One|NonSelectable|Skip chooseSkippableOne(String message, [Item+] items) {
		return nonSelectable;
	}
	shared actual Multiple|NonSelectable|Skip chooseSkippableMulti(String message, [Item+] items) {
		return nonSelectable;
	}
}
