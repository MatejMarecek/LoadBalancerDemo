import React from 'react';
import './App.css';
import { LoadBalancer } from "./components/LoadBalancer";
import Container from 'react-bootstrap/Container';


function App() {

  return (
    <Container className="App" fluid>
      <LoadBalancer/>
    </Container>
  );
}

export default App;
