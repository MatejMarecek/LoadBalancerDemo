import * as React from "react";
import SockJS from 'sockjs-client';
import Stomp, { Frame } from 'stompjs';
import { LoadBalancerState } from "./LoadBalancerState";
import Button from 'react-bootstrap/Button';
import { RequestsDisplay } from "./RequestsDisplay";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Tabs from 'react-bootstrap/Tabs';
import Tab from 'react-bootstrap/Tab';
import Jumbotron from 'react-bootstrap/Jumbotron';
import Badge from 'react-bootstrap/Badge';
import './LoadBalancer.css';
import { LoadBalancerConfig } from "./LoadBalancerConfig";
import { AppConfig } from "../AppConfig";


export interface LoadBalancerProps { }
interface LoadBalancerComponentState {
  latestGetValue?: String
}

export class LoadBalancer extends React.Component<LoadBalancerProps, LoadBalancerComponentState, {}> {

  private stompClient: Stomp.Client;
  constructor(props: LoadBalancerProps) {
    super(props);

    let socket = new SockJS(AppConfig.getWebSocketUrl());
    this.stompClient = Stomp.over(socket);
  }

  componentDidMount() {
    this.stompClient.connect({}, (frame?: Frame) => {});
  }

  componentWillUnmount() {
    this.stompClient.disconnect(() => {});
  }

  onClickGet = async (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => {
    this.stompClient.send("/app/get", {});
  }

  onGetValueChange = async (value: String) => {
    this.setState({latestGetValue : value});
  }

  render() {
    return (
      <Container fluid>
        <Row>
          <Col>
            <Jumbotron>
              <h1>Load Balancer</h1>
              <p>
                This is an application that demonstrates a behavior and usage of a Load Balancer.
                It serves as a front-end for a server running the balancer service.
                You can configure the balancer in the Configuration tab. The options include the scheduling
                algorithm (random/round-robin), simulated delays of the GET call and failure rate
                for Providers.
              </p>
              <p>
                Click the <Badge variant="primary">GET</Badge> button to call the server.
              </p>
              <p>
                <Button size="lg" onClick={this.onClickGet}>GET</Button>
              </p>
              <RequestsDisplay receivedValueCb={this.onGetValueChange} />
            </Jumbotron>
          </Col>
        </Row>
        <Row>
          <Col>
            <Tabs className="LoadBalancer-tab" defaultActiveKey="state" id="mainTab">
              <Tab eventKey="state" title="State">
                <LoadBalancerState highlightProvider={this?.state?.latestGetValue} />
              </Tab>
              <Tab eventKey="configuration" title="Configuration">
                <LoadBalancerConfig />
              </Tab>
            </Tabs>
          </Col>
        </Row>
      </Container>
    );
  }
}