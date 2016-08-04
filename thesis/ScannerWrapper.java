package thesis;

import java.util.Scanner;

public class ScannerWrapper implements Wrapper{
    
    private Scanner sc;
    
    public ScannerWrapper(Scanner sc) {
        this.sc = sc;
    }

    @Override
    public int nextInt(int param) {
        return sc.nextInt(param);
    }

}
