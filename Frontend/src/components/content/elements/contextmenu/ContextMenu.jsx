import React, { Component } from 'react'

import { Link } from "react-router-dom";

export default class ContextMenu extends Component {
        
    componentDidMount() {
        this.props.onStart();
    }

    render() {
        return (
            <div className="context-menu">
                <div onClick={() => this.props.action("download")}>
                    <div className="context-menu-icon">
                        <i className="fas fa-download"/>
                    </div>
                    <span>Download</span>
                </div>
                <div onClick={() => this.props.action("cut")}>
                    <div className="context-menu-icon">
                        <i className="fas fa-cut"/>
                    </div>
                    <span>Cut</span>
                </div>
                <div onClick={() => this.props.action("copy")}>
                    <div className="context-menu-icon">
                        <i className="fas fa-copy"/>
                    </div>
                    <span>Copy</span>
                </div>
                <Link to="/rename" className="context-menu-link">
                    <div className="context-menu-icon">
                        <i className="fas fa-signature"/>
                    </div>
                    <span>Rename</span>
                </Link>
                <div onClick={() => this.props.action("delete")}>
                    <div className="context-menu-icon">
                        <i className="far fa-trash-alt"/>
                    </div>
                    <span>Delete</span>
                </div>
                <Link to="/element-info" className="context-menu-link">
                    <div className="context-menu-icon">
                        <i className="fas fa-info"/>
                    </div> 
                    <span>Info</span> 
                </Link>
            </div>
        );
    }
}