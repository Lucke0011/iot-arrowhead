package ai.aitia.thermostat.service;

import com.opencsv.CSVReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataService {
    private static final String TEMPERATURE_DATA_CSV = "temperature_data.csv";
    private List<Double> temperatureData;

    public List<Double> readTemperatureData() throws IOException {
        final Resource resource = new ClassPathResource(TEMPERATURE_DATA_CSV);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        final CSVReader csvReader = new CSVReader(reader);
        try {
            final List<String[]> readAll = csvReader.readAll();
            temperatureData = new ArrayList<Double>(readAll.size());
            for (final String[] strings : readAll) {
                temperatureData.add(Double.valueOf(strings[0]));
            }
            return temperatureData;
        } catch (final Exception e) {
            throw e;
        } finally {
            reader.close();
            csvReader.close();
        }
    }
}
