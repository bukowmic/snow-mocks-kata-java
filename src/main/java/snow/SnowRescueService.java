package snow;

import snow.dependencies.MunicipalServices;
import snow.dependencies.PressService;
import snow.dependencies.SnowplowMalfunctioningException;
import snow.dependencies.WeatherForecastService;

public class SnowRescueService {
    private final WeatherForecastService weatherForecastService;
    private final MunicipalServices municipalServices;
    private final PressService pressService;

    public SnowRescueService(WeatherForecastService weatherForecastService, MunicipalServices municipalServices, PressService pressService) {
        this.weatherForecastService = weatherForecastService;
        this.municipalServices = municipalServices;
        this.pressService = pressService;
    }

    public void checkForecastAndRescue() {
        int averageTemperatureInCelsius = weatherForecastService.getAverageTemperatureInCelsius();
        int snowFallHeightInMM = weatherForecastService.getSnowFallHeightInMM();

        if (shouldSendSander(averageTemperatureInCelsius)) {
            municipalServices.sendSander();
        }

        if (shouldSendFirstSnowplow(snowFallHeightInMM)) {
            sendSnowplow();
        }

        if (shouldSendSecondSnowplow(snowFallHeightInMM)) {
            sendSnowplow();
        }

        if (shouldSendThirdSnowplowAndCallPress(averageTemperatureInCelsius, snowFallHeightInMM)) {
            sendSnowplow();
            pressService.sendWeatherAlert();
        }
    }

    private boolean shouldSendThirdSnowplowAndCallPress(int averageTemperatureInCelsius, int snowFallHeightInMM) {
        return snowFallHeightInMM > 10 && averageTemperatureInCelsius < 10;
    }

    private boolean shouldSendSecondSnowplow(int snowFallHeightInMM) {
        return snowFallHeightInMM > 5;
    }

    private boolean shouldSendFirstSnowplow(int snowFallHeightInMM) {
        return snowFallHeightInMM > 3;
    }

    private boolean shouldSendSander(int averageTemperatureInCelsius) {
        return averageTemperatureInCelsius < 0;
    }

    private void sendSnowplow() {
        try {
            municipalServices.sendSnowplow();
        } catch (SnowplowMalfunctioningException ex) {
            municipalServices.sendSnowplow();
        }
    }

}
