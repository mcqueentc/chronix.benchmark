import database.BenchmarkDataSource;

/**
 * Created by mcqueen666 on 08.08.16.
 *
 * Interface implementation for Chronix DB
 *
 */

//TODO after testing, link this entire module as dependency to the client!!!

public class Chronix implements BenchmarkDataSource{

    @Override
    public boolean ping() {
        return true;
    }
}
