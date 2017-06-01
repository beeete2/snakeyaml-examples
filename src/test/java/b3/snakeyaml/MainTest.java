package b3.snakeyaml;

import b3.snakeyaml.entities.Host;
import b3.snakeyaml.entities.Item;
import b3.snakeyaml.entities.Server;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private Main main;

    @Before
    public void setUp() throws Exception {
        main = new Main();
    }

    @Test
    public void load() throws Exception {
        String text = readDefaultText();

        Yaml yaml = new Yaml();
        Map map = (Map) yaml.load(text);

        softly.assertThat(getString(map, "name")).isEqualTo("server1");

        List hosts = (List) map.get("hosts");
        softly.assertThat(hosts).hasSize(1);

        Map host = (Map) hosts.get(0);
        softly.assertThat(getString(host, "name")).isEqualTo("host1");

        List items = (List) host.get("items");
        softly.assertThat(items).hasSize(2);

        Map item = (Map) items.get(0);
        softly.assertThat(getString(item, "key")).isEqualTo("item1");
        softly.assertThat(item.get("startDate")).isInstanceOf(Date.class); // 日付はDateオブジェクトになる
    }

    @Test
    public void loadAs() throws Exception {
        String text = readDefaultText();

        Server server = main.loadAs(text, Server.class);
        softly.assertThat(server.name).isEqualTo("server1");
        softly.assertThat(server.hosts).hasSize(1);

        Host host = server.hosts.get(0);
        softly.assertThat(host.name).isEqualTo("host1");
        softly.assertThat(host.items).hasSize(2);

        Item item = host.items.get(0);
        softly.assertThat(dateFormat(item.startDate)).isEqualTo("2017-05-01T00:00:00"); // LocalDateTimeになる
        softly.assertThat(dateFormat(item.endDate)).isEqualTo("2017-05-31T00:00:00");// LocalDateTimeになる
    }

    @Test
    public void dumpOnDefault() throws Exception {
        Server server = createServer();
        Yaml yaml = new Yaml();
        String text = yaml.dump(server);
        softly.assertThat(text).isNotNull();
    }

    @Test
    public void dump() throws Exception {
        Server server = createServer();
        String text = main.dump(server);
        softly.assertThat(text).isNotNull();
    }

    private Server createServer() {
        List<Item> items = Arrays.asList(
                createItem("1", "item1"),
                createItem("2", "item2")
        );

        Host host = new Host();
        host.hostid = "1";
        host.name = "host1";
        host.items = items;
        List<Host> hosts = Arrays.asList(
                host
        );

        Server server = new Server();
        server.name = "server1";
        server.hosts = hosts;

        return server;
    }

    private Item createItem(String itemid, String key) {
        Item item = new Item();
        item.itemid = itemid;
        item.key = key;
        item.startDate = LocalDateTime.of(2016, 05, 1, 0, 0, 0);
        item.endDate = LocalDateTime.of(2016, 05, 31, 0, 0, 0);

        return item;
    }

    private String resourceToString(String path) throws IOException {
        InputStream input = null;
        try {
            input = this.getClass().getClassLoader().getResourceAsStream("servers1.yml");
            return IOUtils.toString(input, StandardCharsets.UTF_8);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    private String readDefaultText() throws IOException {
        String text = resourceToString("servers1.yml");
        return text;
    }

    private static String dateFormat(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private static String getString(Map map, String key) {
        Object value = map.get(key);
        if (value != null) {
            return value.toString();
        }

        return null;
    }
}