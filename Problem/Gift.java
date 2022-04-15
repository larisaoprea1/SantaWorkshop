import java.io.Serializable;

public class Gift implements Serializable {
 int serial;
 int elf_serial;
 
public Gift(int elf_serial, int serial) {
	this.elf_serial = elf_serial;
	this.serial = serial;
}


}