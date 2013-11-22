<spirit:design xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:spirit="http://www.spiritconsortium.org/XMLSchema/SPIRIT/1.4"
               xsi:schemaLocation="http://www.spiritconsortium.org/XMLSchema/SPIRIT/1.4 http://www.spiritconsortium.org/XMLSchema/SPIRIT/1.4/index.xsd">
    <spirit:vendor>ARCINTERNATIONAL</spirit:vendor>
    <spirit:library>hwarch_lib</spirit:library>
    <spirit:name>hwarch</spirit:name>
    <spirit:version>1.0</spirit:version>
    <spirit:componentInstances>
        <spirit:componentInstance>
            <spirit:instanceName>cpu0</spirit:instanceName>
            <spirit:componentRef spirit:vendor="ARCINTERNATIONAL" spirit:library="VDK"
                                 spirit:name="ARC700" spirit:version="0.1"/>
        </spirit:componentInstance>
        <spirit:componentInstance>
            <spirit:instanceName>cpu1</spirit:instanceName>
            <spirit:componentRef spirit:vendor="ARCINTERNATIONAL" spirit:library="VDK"
                                 spirit:name="ARC700" spirit:version="0.1"/>
        </spirit:componentInstance>
        <spirit:componentInstance>
            <spirit:instanceName>cpu2</spirit:instanceName>
            <spirit:componentRef spirit:vendor="ARCINTERNATIONAL" spirit:library="VDK"
                                 spirit:name="ARC700" spirit:version="0.1"/>
        </spirit:componentInstance>
        <spirit:componentInstance>
            <spirit:instanceName>cpu3</spirit:instanceName>
            <spirit:componentRef spirit:vendor="ARCINTERNATIONAL" spirit:library="VDK"
                                 spirit:name="ARC700" spirit:version="0.1"/>
        </spirit:componentInstance>
        <spirit:componentInstance>
            <spirit:instanceName>arbInst</spirit:instanceName>
            <spirit:componentRef spirit:vendor="ARCINTERNATIONAL" spirit:library="VDK"
                                 spirit:name="Arbiter" spirit:version="1.0"/>
        </spirit:componentInstance>
        <spirit:componentInstance>
            <spirit:instanceName>bridgeTopInst</spirit:instanceName>
            <spirit:componentRef spirit:vendor="ARCINTERNATIONAL" spirit:library="VDK"
                                 spirit:name="bridgetop" spirit:version="1.0"/>
        </spirit:componentInstance>
        <spirit:componentInstance>
            <spirit:instanceName>uart0</spirit:instanceName>
            <spirit:componentRef spirit:vendor="ARCINTERNATIONAL" spirit:library="VDK"
                                 spirit:name="BVCIUART" spirit:version="1.1"/>
        </spirit:componentInstance>
        <spirit:componentInstance>
            <spirit:instanceName>ssramControllerInst</spirit:instanceName>
            <spirit:componentRef spirit:vendor="ARCINTERNATIONAL" spirit:library="VDK"
                                 spirit:name="ssramcontroller" spirit:version="1.0"/>
        </spirit:componentInstance>
        <spirit:componentInstance>
            <spirit:instanceName>ssramInst</spirit:instanceName>
            <spirit:componentRef spirit:vendor="ARCINTERNATIONAL" spirit:library="VDK"
                                 spirit:name="SSRAM" spirit:version="1.1"/>
        </spirit:componentInstance>
    </spirit:componentInstances>
    <spirit:interconnections>
        <spirit:interconnection>
            <spirit:name>instruction_bus0</spirit:name>
            <spirit:activeInterface spirit:componentRef="cpu0" spirit:busRef="InstructionInterface"/>
            <spirit:activeInterface spirit:componentRef="arbInst" spirit:busRef="IBus0"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>data_bus0</spirit:name>
            <spirit:activeInterface spirit:componentRef="cpu0" spirit:busRef="DataInterface"/>
            <spirit:activeInterface spirit:componentRef="arbInst" spirit:busRef="DBus0"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>instruction_bus1</spirit:name>
            <spirit:activeInterface spirit:componentRef="cpu1" spirit:busRef="InstructionInterface"/>
            <spirit:activeInterface spirit:componentRef="arbInst" spirit:busRef="IBus1"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>data_bus1</spirit:name>
            <spirit:activeInterface spirit:componentRef="cpu1" spirit:busRef="DataInterface"/>
            <spirit:activeInterface spirit:componentRef="arbInst" spirit:busRef="DBus1"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>instruction_bus2</spirit:name>
            <spirit:activeInterface spirit:componentRef="cpu2" spirit:busRef="InstructionInterface"/>
            <spirit:activeInterface spirit:componentRef="arbInst" spirit:busRef="IBus2"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>data_bus2</spirit:name>
            <spirit:activeInterface spirit:componentRef="cpu2" spirit:busRef="DataInterface"/>
            <spirit:activeInterface spirit:componentRef="arbInst" spirit:busRef="DBus2"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>instruction_bus3</spirit:name>
            <spirit:activeInterface spirit:componentRef="cpu3" spirit:busRef="InstructionInterface"/>
            <spirit:activeInterface spirit:componentRef="arbInst" spirit:busRef="IBus3"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>data_bus3</spirit:name>
            <spirit:activeInterface spirit:componentRef="cpu3" spirit:busRef="DataInterface"/>
            <spirit:activeInterface spirit:componentRef="arbInst" spirit:busRef="DBus3"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>arbiter2bridge</spirit:name>
            <spirit:activeInterface spirit:componentRef="arbInst" spirit:busRef="perhipheralBridge"/>
            <spirit:activeInterface spirit:componentRef="bridgeTopInst" spirit:busRef="BridgeTopTarget"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>bridge2uart</spirit:name>
            <spirit:activeInterface spirit:componentRef="uart0" spirit:busRef="HostInterface"/>
            <spirit:activeInterface spirit:componentRef="bridgeTopInst" spirit:busRef="HostInterface"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>arbiter2ssramController</spirit:name>
            <spirit:activeInterface spirit:componentRef="arbInst" spirit:busRef="ssramMemController"/>
            <spirit:activeInterface spirit:componentRef="ssramControllerInst" spirit:busRef="SramInterface"/>
        </spirit:interconnection>
        <spirit:interconnection>
            <spirit:name>ssramController2sram</spirit:name>
            <spirit:activeInterface spirit:componentRef="ssramControllerInst" spirit:busRef="PhysicalSSRAM"/>
            <spirit:activeInterface spirit:componentRef="ssramInst" spirit:busRef="PhysicalSSRAM"/>
        </spirit:interconnection>
    </spirit:interconnections>
</spirit:design>
