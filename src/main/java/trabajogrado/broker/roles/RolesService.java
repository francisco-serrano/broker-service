package trabajogrado.broker.roles;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import trabajogrado.broker.configuration.MicroservicesConfiguration;

import java.util.ArrayList;
import java.util.List;

@Service
public class RolesService {

    private static final String GET_ROLES_URL = "http://%s:%s/obtener_roles?ipa_arrays=%s";

    private MicroservicesConfiguration configuration;
    private String address;
    private int port;

    @Autowired
    public RolesService(MicroservicesConfiguration configuration) {
        this.configuration = configuration;
        this.address = configuration.dockerized() ? configuration.getRolesAddressDocker() : configuration.getRolesAddress();
        this.port = configuration.getRolesPort();
    }

    public String getRolesFromJson(String jsonClassification) {


        List<String> listaConductas = new ArrayList<>();
        JsonArray jsonArray = new JsonParser().parse(jsonClassification).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject mapeoPersonaClasificacion = jsonArray.get(i).getAsJsonObject().getAsJsonObject("mapeoPersonaClasificacion");

            mapeoPersonaClasificacion.keySet().forEach(user -> {

                // Obtengo las conductas
                String conductas = mapeoPersonaClasificacion
                        .getAsJsonObject(user)
                        .getAsJsonArray("conductas").toString()
                        .replaceAll("(\\[)|(\\])", "");

                listaConductas.add(conductas);
            });
        }

        String url = String.format(GET_ROLES_URL, address, port, Joiner.on(';').join(listaConductas).toString().replaceAll("(\\[)|(\\])", ""));

        String rolesModificados = null;

        try {
            rolesModificados = Unirest.get(url).asString().getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        assert rolesModificados != null;

        List<String> rolesInsertar = new ArrayList<>(Splitter.on("], [").splitToList(rolesModificados.replaceAll("(\\[\\[)|(\\]\\])", "")));
        
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject mapeoPersonaClasificacion = jsonArray.get(i).getAsJsonObject().getAsJsonObject("mapeoPersonaClasificacion");

            mapeoPersonaClasificacion.keySet().forEach(user -> {
                List<String> rolesInsertarPersona = Splitter.on(", ").splitToList(rolesInsertar.remove(0));

                // Creo el arreglo con los roles nuevos
                JsonArray roles = new JsonArray();
                for (int j = 0; j < 9; j++)
                    roles.add(Float.parseFloat(rolesInsertarPersona.get(j)));

                // Inserto el arreglo nuevo, reemplazando el anterior
                mapeoPersonaClasificacion.getAsJsonObject(user).add("roles", roles);
            });
        }

        return jsonArray.toString();
    }
}
