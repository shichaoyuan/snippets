
import com.google.common.base.Optional;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuan.shichao
 */
public class JMXUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JMXUtils.class);

    private static final String KAFKA_NAME = "kafka.Kafka";

    /**
     * 获取本地VirtualMachineDescriptor
     *
     * @return
     */
    public static List<VirtualMachineDescriptor> localVMs() {
        return VirtualMachine.list();
    }

    /**
     * 获取kafka的vmd
     * @return
     */
    public static Optional<VirtualMachineDescriptor> getKafkaVMD() {
        List<VirtualMachineDescriptor> vms = localVMs();
        for (VirtualMachineDescriptor vm : vms) {
            if (vm.displayName().startsWith(KAFKA_NAME)) {
                return Optional.of(vm);
            }
        }
        return Optional.absent();
    }

    /**
     * 通过host:port地址获取JMXConnector
     *
     * @param addr
     * @param credentialsOpt
     * @return
     */
    public static Optional<JMXConnector> remoteConnect(String addr, Optional<JMXCredentials> credentialsOpt) {
        Map<String, String[]> ht = null;
        if (credentialsOpt.isPresent()) {
            JMXCredentials credentials = credentialsOpt.get();
            ht = Collections.singletonMap(JMXConnector.CREDENTIALS,
                    new String[] {credentials.username, credentials.password});
        }

        try {
            JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(addr), ht);
            return Optional.of(connector);
        } catch (Exception e) {
            LOGGER.error("connect to {} @ {} failed", addr, credentialsOpt.orNull());
            return Optional.absent();
        }
    }

    /**
     * 通过本地vmd获取JMXConnector
     *
     * @param vmd
     * @return
     */
    public static Optional<JMXConnector> localConnect(VirtualMachineDescriptor vmd) {
        if (vmd == null) {
            return Optional.absent();
        }

        String localConnectorAddress = null;
        try {
            VirtualMachine vm = VirtualMachine.attach(vmd);
            localConnectorAddress = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
            if (localConnectorAddress == null) {
                String agent = vm.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar";
                vm.loadAgent(agent);
                localConnectorAddress = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
            }
        } catch (Exception e) {
            LOGGER.error("get localConnectorAddress failed", e);
        }

        if (localConnectorAddress == null) {
            return Optional.absent();
        }

        try {
            JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(localConnectorAddress));
            return Optional.of(connector);
        } catch (Exception e) {
            LOGGER.error("connect to {} failed", vmd, e);
            return Optional.absent();
        }
    }

    /**
     * 通过本地vmid获取JMXConnector
     *
     * @param vmid
     * @return
     */
    public static Optional<JMXConnector> localConnect(String vmid) {
        List<VirtualMachineDescriptor> localVms = VirtualMachine.list();
        VirtualMachineDescriptor vmd = null;
        for (VirtualMachineDescriptor item : localVms) {
            if (item.id().equals(vmid)) {
                vmd = item;
                break;
            }
        }
        if (vmd == null) {
            LOGGER.error("vmid {} not found", vmid);
            return Optional.absent();
        }

        return localConnect(vmd);

    }

    /**
     * 获取MBeanServerConnection
     *
     * @param connector
     * @return
     */
    public static Optional<MBeanServerConnection> getConnection(JMXConnector connector) {
        if (connector == null) {
            return Optional.absent();
        }

        try {
            MBeanServerConnection conn = connector.getMBeanServerConnection();
            return Optional.of(conn);
        } catch (Exception e) {
            LOGGER.error("getMBeanServerConnection failed", e);
            return Optional.absent();
        }
    }

    /**
     * 获取所有MBeans
     *
     * @param conn
     * @return
     */
    public static Set<ObjectInstance> getAllMBeans(MBeanServerConnection conn) {
        try {
            return conn.queryMBeans(null, null);
        } catch (Exception e) {
            LOGGER.error("queryMBeans failed", e);
            return Collections.emptySet();
        }
    }

    /**
     * 获取MBean的所有属性
     *
     * @param conn
     * @param objectName
     * @return
     */
    public static List<Attribute> getAttributeList(MBeanServerConnection conn, ObjectName objectName) {
        try {
            MBeanInfo mBeanInfo = conn.getMBeanInfo(objectName);
            MBeanAttributeInfo[] attributeInfoArray = mBeanInfo.getAttributes();

            String[] attributeStrArray = new String[attributeInfoArray.length];
            for (int i = 0; i < attributeInfoArray.length; i++) {
                attributeStrArray[i] = attributeInfoArray[i].getName();
            }

            return conn.getAttributes(objectName, attributeStrArray).asList();
        } catch (Exception e) {
            LOGGER.error("getAttributeList failed [objectName: {}]", objectName, e);
            return Collections.emptyList();
        }
    }

    /**
     * 通过name获取属性value
     *
     * @param attributeList
     * @param name
     * @return
     */
    public static Object getValue(List<Attribute> attributeList, String name) {
        for (Attribute attr : attributeList) {
            if (attr.getName().equals(name)) {
                return attr.getValue();
            }
        }
        return null;
    }

    public static class JMXCredentials {
        private final String username;
        private final String password;

        public JMXCredentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("JMXCredentials{");
            sb.append("username='").append(username).append('\'');
            sb.append(", password='").append(password).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
