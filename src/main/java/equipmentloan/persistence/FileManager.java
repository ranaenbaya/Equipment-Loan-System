package equipmentloan.persistence;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Handles serialising and deserialising application data.
 */
public final class FileManager {
    private FileManager() {
    }

    public static void save(String file, Object obj) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(obj);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save data to '" + file + "'.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T load(String file) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (T) in.readObject();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load data from '" + file + "'.", e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Saved data format is invalid.", e);
        }
    }
}
