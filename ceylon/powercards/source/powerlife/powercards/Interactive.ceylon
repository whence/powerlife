import powerlife.powercards.choices {
    One,
    NonSelectable,
    nonSelectable,
    Skip,
    skip,
    Multiple
}

shared interface Interactive {
    shared formal IO io;
    
    shared One|NonSelectable chooseOne(String message, [Item+] items) {
        return nonSelectable;
    }
    
    shared One|NonSelectable|Skip chooseSkippableOne(String message, [Item+] items) {
        return skip;
    }
    
    shared Multiple|NonSelectable|Skip chooseSkippableMulti(String message, [Item+] items) {
        return Multiple([0]);
    }
}
