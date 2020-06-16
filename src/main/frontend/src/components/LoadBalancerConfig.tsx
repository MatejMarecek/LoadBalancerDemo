import * as React from "react";
import Container from 'react-bootstrap/Container';
import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";
import Badge from "react-bootstrap/Badge";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import ButtonGroup from "react-bootstrap/ButtonGroup";
import './LoadBalancerConfig.css';
import { AppConfig } from "../AppConfig";

export interface LoadBalancerConfigProps {}

export interface LoadBalancerConfigState {
  loadBalancer: LoadBalancerPublicConfig;
  provider: DefaultProviderPublicConfig;
}

export interface LoadBalancerPublicConfig {
  algorithm?: ('RANDOM' | 'ROUND_ROBIN');
  maxProviderRequests?: number;
}

export interface DefaultProviderPublicConfig {
  minGetDelayMs?: number;
  maxGetDelayMs?: number;
  minCheckDelayMs?: number;
  maxCheckDelayMs?: number;
  checkFailPercentage?: number;
  recoverTimeMs?: number;
}

export class LoadBalancerConfig extends React.Component<LoadBalancerConfigProps, LoadBalancerConfigState, {}> {

  constructor(props: LoadBalancerConfigProps) {
    super(props);

    this.state = { loadBalancer: {}, provider: {} }
  }

  componentDidMount() {
    this.reloadConfig();
  }

  // ----------------------------
  // --- Reload Configuration ---
  // ----------------------------
  onReload = async () => {
    this.reloadConfig()
  }

  reloadConfig = async () => {
    fetch(AppConfig.getLoadBalancerConfigUrl())
      .then(response => response.json())
      .then(result => this.setState({ loadBalancer: result }))
      .catch(error => console.log('error', error));

    fetch(AppConfig.getProviderConfigUrl())
      .then(response => response.json())
      .then(result => this.setState({ provider: result }))
      .catch(error => console.log('error', error));
  }

  onReset = async () => {
    fetch(AppConfig.getLoadBalancerConfigUrl(), { method: 'DELETE' })
      .then(response => response.json())
      .then(result => this.setState({ loadBalancer: result }))
      .catch(error => console.log('error', error));

    fetch(AppConfig.getProviderConfigUrl(), { method: 'DELETE' })
      .then(response => response.json())
      .then(result => this.setState({ provider: result }))
      .catch(error => console.log('error', error));
  }

  // ---------------------------
  // --- Apply Configuration ---
  // ---------------------------
  onApply = async () => {
    this.applyConfig(this.state);
  }

  applyConfig = async (dataToApply: LoadBalancerConfigState) => {
    this.applyLoadBalancerConfig(dataToApply.loadBalancer);
    this.applyProviderConfig(dataToApply.provider);
  }

  applyLoadBalancerConfig = async (dataToApply: LoadBalancerPublicConfig) => {
    fetch(AppConfig.getLoadBalancerConfigUrl(), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dataToApply),
      redirect: 'follow'
    })
      .then(response => response.json())
      .then(result => this.setState({ loadBalancer: result }))
      .catch(error => console.log('error', error));
  }

  applyProviderConfig = async (dataToApply: DefaultProviderPublicConfig) => {
    fetch(AppConfig.getProviderConfigUrl(), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dataToApply),
      redirect: 'follow'
    })
      .then(response => response.json())
      .then(result => this.setState({ provider: result }))
      .catch(error => console.log('error', error));
  }

  // -----------------------------
  // --- Form change callbacks ---
  // -----------------------------
  onAlgorithChange = (e: any) => {
    var loadBalancer = this.state.loadBalancer
    loadBalancer.algorithm = e.target.value;
    this.setState({ loadBalancer: loadBalancer })
  }

  onMaxProviderRequestsChange = (e: any) => {
    var loadBalancer = this.state.loadBalancer
    loadBalancer.maxProviderRequests = e.target.value;
    this.setState({ loadBalancer: loadBalancer })
  }

  onMinGetDelayMsChange = (e: any) => {
    var provider = this.state.provider
    provider.minGetDelayMs = e.target.value;
    this.setState({ provider: provider })
  }

  onMaxGetDelayMsChange = (e: any) => {
    var provider = this.state.provider
    provider.maxGetDelayMs = e.target.value;
    this.setState({ provider: provider })
  }

  onMinCheckDelayMsChange = (e: any) => {
    var provider = this.state.provider
    provider.minCheckDelayMs = e.target.value;
    this.setState({ provider: provider })
  }

  onMaxCheckDelayMsChange = (e: any) => {
    var provider = this.state.provider
    provider.maxCheckDelayMs = e.target.value;
    this.setState({ provider: provider })
  }

  onCheckFailPercentageChange = (e: any) => {
    var provider = this.state.provider
    provider.checkFailPercentage = e.target.value;
    this.setState({ provider: provider })
  }

  onRecoverTimeMsChange = (e: any) => {
    var provider = this.state.provider
    provider.recoverTimeMs = e.target.value;
    this.setState({ provider: provider })
  }


  render() {
    if (this?.state?.loadBalancer?.algorithm === undefined ||
      this?.state?.provider?.checkFailPercentage === undefined) {
      return (<p>Loading data...</p>);
    }

    let loadBalancer = this.state.loadBalancer;
    let provider = this.state.provider;

    return (
      <Container fluid>
        <Form>
          <Form.Group>
            <Form.Label>Algorithm</Form.Label>
            <Form.Control as="select" id="algorithmSelect" custom value={loadBalancer.algorithm} onChange={this.onAlgorithChange}>
              <option value="RANDOM">Random</option>
              <option value="ROUND_ROBIN">Round robin</option>
            </Form.Control>
          </Form.Group>
          <Form.Group >
            <Form.Label>Max requests per provider <Badge variant="primary">{loadBalancer.maxProviderRequests}</Badge></Form.Label>
            <Form.Control type="range" min={0} max={20} value={loadBalancer.maxProviderRequests} onChange={this.onMaxProviderRequestsChange} />
          </Form.Group>

          <Row>
            <Col>
              <h5 className="LoadBalancerConfig-from-section-header">GET delay</h5>
              <p className="LoadBalancerConfig-from-section-description">
                When the GET method of a Default Provider is called, the work done is simulated by an artificial random delay.
                This range defines the minimum and maximum random values in milliseconds.
              </p>
            </Col>
          </Row>
          <Row>
            <Col>
              <Form.Group >
                <Form.Label>Min GET delay <Badge variant="primary">{provider.minGetDelayMs}</Badge> ms</Form.Label>
                <Form.Control type="range" min={0} max={provider.maxGetDelayMs} value={provider.minGetDelayMs} onChange={this.onMinGetDelayMsChange} />
              </Form.Group>
            </Col>
            <Col>
              <Form.Group >
                <Form.Label>Max GET delay <Badge variant="primary">{provider.maxGetDelayMs}</Badge> ms</Form.Label>
                <Form.Control type="range" min={provider.minGetDelayMs} max={10000} value={provider.maxGetDelayMs} onChange={this.onMaxGetDelayMsChange} />
              </Form.Group>
            </Col>
          </Row>

          <Row>
            <Col >
              <h5 className="LoadBalancerConfig-from-section-header">Health check delay</h5>
              <p className="LoadBalancerConfig-from-section-description">
                The load balancer performs periodic checks (every 7 seconds) of all the Providers.
                The limits for the random delay inside the check method is defined here. The unit is millisecond.
              </p>
            </Col>
          </Row>
          <Row>
            <Col>
              <Form.Group >
                <Form.Label>Min check delay <Badge variant="primary">{provider.minCheckDelayMs}</Badge> ms</Form.Label>
                <Form.Control type="range" min={0} max={provider.maxCheckDelayMs} value={provider.minCheckDelayMs} onChange={this.onMinCheckDelayMsChange} />
              </Form.Group>
            </Col>
            <Col>
              <Form.Group >
                <Form.Label>Max check delay <Badge variant="primary">{provider.maxCheckDelayMs}</Badge> ms</Form.Label>
                <Form.Control type="range" min={provider.minCheckDelayMs} max={10000} value={provider.maxCheckDelayMs} onChange={this.onMaxCheckDelayMsChange} />
              </Form.Group>
            </Col>
          </Row>

          <Row>
            <Col>
              <h5 className="LoadBalancerConfig-from-section-header">Provider health check</h5>
              <p className="LoadBalancerConfig-from-section-description">
                This parameter is used to simulate the failures of Providers.
                When a check of a Provider is performed, there a X% chance that it fails.
              </p>
            </Col>
          </Row>
          <Row>
            <Col>
              <Form.Group >
                <Form.Label>Check error probability <Badge variant="primary">{provider.checkFailPercentage}</Badge> %</Form.Label>
                <Form.Control type="range" min={0} max={100} value={provider.checkFailPercentage} onChange={this.onCheckFailPercentageChange} />
              </Form.Group>
            </Col>
          </Row>

          <Row>
            <Col>
              <h5 className="LoadBalancerConfig-from-section-header">Failure recovery</h5>
              <p className="LoadBalancerConfig-from-section-description">
                The value here defines the time in milliseconds for which the Provider stays in the fail state after its failure.
              </p>
            </Col>
          </Row>
          <Row>
            <Col>
              <Form.Group >
                <Form.Label>Remover time <Badge variant="primary">{provider.recoverTimeMs}</Badge> ms</Form.Label>
                <Form.Control type="range" min={0} max={60000} value={provider.recoverTimeMs} onChange={this.onRecoverTimeMsChange} />
              </Form.Group>
            </Col>
          </Row>

          <Row>
            <Col className="text-center">
              <ButtonGroup aria-label="Basic example">
                <Button variant="secondary" onClick={this.onReset}>Reset</Button>
                <Button variant="primary" onClick={this.onApply}>Apply</Button>
              </ButtonGroup>
            </Col>
          </Row>
        </Form>
      </Container>
    );
  }
}