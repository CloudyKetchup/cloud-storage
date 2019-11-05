import React, { Component } from 'react';
import ReactDOM             from 'react-dom';

import App                      from './App';
import { APIHelpers as API }    from './helpers';
import { OfflineScreen }        from './components/OfflineScreen/OfflineScreen';
import { CircularProgress }     from '@material-ui/core';
import offlineSVG               from "./components/OfflineScreen/offline.svg";

import './css/index.css';

class AppWrapper extends Component {
    state = { data : null, loading : true, offline : false };

    componentWillMount = () => this.updateRootFolder();

    componentDidMount = () => {
        if (!this.state.data) {
            setTimeout(() => {
                setInterval(async () => !this.state.loading && !this.state.data && await this.updateRootFolder(), 5000);
            }, 5);
        }
    };

    updateRootFolder = () => {
        this.setState({ loading : true });

        API.getRootData().then(root => {
            if (root)
                this.setState({ data: root, loading: false, offline: false });
            else
                this.setState({ loading: false, offline: true });
        });
    };

    render = () => (
        [
            this.state.data
            &&
            <App data={this.state.data}/>,
            !this.state.data
            &&
            <OfflineScreen img={offlineSVG} text={this.state.offline && "Server Offline :("}>
                <CircularProgress style={{ color: "white" }}/>
            </OfflineScreen>
        ]
    );
}

ReactDOM.render(<AppWrapper/>, document.getElementById('root'));