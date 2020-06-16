import * as React from "react";
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import Badge from 'react-bootstrap/Badge';
import { AppConfig } from "../AppConfig";

export interface RequestsDisplayProps {
  label?: String;
  initValue?: String;
  receivedValueCb?: (value : String) => void
}

export interface RequestsDisplayData {
  receivedValue: String;
}

export class RequestsDisplay extends React.Component<RequestsDisplayProps, RequestsDisplayData, {}> {

  private stompClient : Stomp.Client;

  constructor(props: RequestsDisplayProps) {
    super(props);

    this.state = {
      receivedValue : props?.initValue ?? 'NONE'
    }

    let socket = new SockJS(AppConfig.getWebSocketUrl());
    this.stompClient = Stomp.over(socket);
  }

  componentDidMount() {
    this.stompClient.connect({}, () => {
      this.stompClient.subscribe('/topic/get', (response) => {
        this.updateGetResponse(response.body);
      });
    });
  }

  componentWillUnmount() {
    this.stompClient.disconnect(() => {});
  }

  updateGetResponse(value : String) {
    this.setState({receivedValue : value})
    if (this.props?.receivedValueCb !== undefined) {
      this.props.receivedValueCb(value);
    }
  }

  render() {
    let data = this.props;
    let label = data?.label ?? 'Latest value';

    return(
      <div>
        <h4>{label} <Badge variant="secondary">{this.state.receivedValue}</Badge></h4>
      </div>
    );
  }
}