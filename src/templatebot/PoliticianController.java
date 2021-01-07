package templatebot;

import battlecode.common.*;
import controllers.CustomPoliticianController;

public class PoliticianController extends CustomPoliticianController {

    public PoliticianController() {

    }

    // This cannot be moved into CustomPoliticianController, because it depends
    // on your actual implementation of PoliticianController, which is not
    // accessible from the abstract class. Keep it, but add to as necessary.
    public PoliticianController(SlandererController sc) {
        super(sc);
    }

    @Override
    public void doTurn() throws GameActionException {

    }
}
