import * as React from "react";
import SockJS from 'sockjs-client';
import Stomp, { Frame } from 'stompjs';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Badge from 'react-bootstrap/Badge';
import ListGroup from "react-bootstrap/ListGroup";
import { AppConfig } from "../AppConfig";

export interface LoadBalancerStateProps {
  highlightProvider?: ProviderId;
}

export type ProviderId = String;
export interface LoadBalancerStateData {
  ongoingRequests: number;
  liveProviders : Array<ProviderId>;
  brokenProviders : Array<ProviderId>;
  recoveredProviders : Array<ProviderId>;
}


export class LoadBalancerState extends React.Component<LoadBalancerStateProps, LoadBalancerStateData, {}> {

  private stompClient : Stomp.Client;

  constructor(props: LoadBalancerStateProps) {
    super(props);

    var socket = new SockJS(AppConfig.getWebSocketUrl());
    this.stompClient = Stomp.over(socket);
  }

  componentDidMount() {
    this.stompClient.connect({}, (frame?: Frame) => {
        this.stompClient.subscribe('/topic/loadbalancer/state', (loadBalancerState) => {
          this.updateLoadBalancerState(JSON.parse(loadBalancerState.body));
        });
      });
  }

  componentWillUnmount() {
    this.stompClient.disconnect(() => {});
  }

  updateLoadBalancerState(data : LoadBalancerStateData) {
    this.setState(data)
  }

  render() {
    if (this.state === null) {
      return(<p>Loading state...</p>);
    }
    let data = this.state;
    let highlightProvider = this.props?.highlightProvider

    let liveProviders = data.liveProviders
      .map((id) => this.renderProvider(id, 'live', id === highlightProvider));
    let recoveredProviders = data.recoveredProviders
      .map((id) => this.renderProvider(id, 'recovered', id === highlightProvider));
    let brokenProviders = data.brokenProviders
      .map((id) => this.renderProvider(id, 'broken', id === highlightProvider));

    return(
      <Container fluid>
          <Row>
            <Col>
              <h4>Ongoing requests <Badge variant='secondary'>{data.ongoingRequests}</Badge></h4>
            </Col>
          </Row>
          <Row>
            <Col>
              <h5>Live providers <Badge variant='secondary'>{liveProviders.length}</Badge></h5>
              <ListGroup>{liveProviders}</ListGroup>
            </Col>
            <Col>
              <h5>Recovered providers <Badge variant='secondary'>{recoveredProviders.length}</Badge></h5>
              <ListGroup>{recoveredProviders}</ListGroup>
            </Col>
            <Col>
              <h5>Broken providers <Badge variant='secondary'>{brokenProviders.length}</Badge></h5>
              <ListGroup>{brokenProviders}</ListGroup>
            </Col>
          </Row>
      </Container>
    );
  }

  renderProvider(id : String, variant : ('live' | 'recovered' | 'broken'), highlight : boolean) {
    var visualVariant = this.providerVariantToVisual(variant)
    return(
      <ListGroup.Item active={highlight} variant={visualVariant} key={id.toString()}>{id}</ListGroup.Item>
    )
  }

  providerVariantToVisual(variant : ('live' | 'recovered' | 'broken')) : ('success' | 'warning' | 'danger') {
    switch(variant) {
      case 'live' : return 'success'
      case 'recovered' : return 'warning'
      case 'broken' : return 'danger'
    };
  }
}