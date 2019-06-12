import React, { Component } from 'react'

export default class ContextMenu extends Component {
    
    componentDidMount() {
        this.props.onStart();
    }

    render() {
        return (
            <div className="context-menu">
                <div onClick={() => this.props.action("cut")}>
                    <div>
                        <i className="fas fa-cut"/>
                    </div>
                    <span>Cut</span>
                </div>
                <div onClick={() => this.props.action("copy")}>
                    <div>
                        <i className="fas fa-copy"/>
                    </div>
                    <span>Copy</span>
                </div>
                <div onClick={() => this.props.action("rename")}>
                    <div>
                        <i className="fas fa-signature"/>
                    </div>
                    <span>Rename</span>
                </div>
                <div onClick={() => this.props.action("delete")}>
                    <div>
                        <i className="far fa-trash-alt"/>
                    </div>
                    <span>Delete</span>
                </div>
                <div onClick={() => this.props.action("info")}>
                    <div>
                        <i className="fas fa-info"/>
                    </div>
                    <span>Info</span>
                </div>
            </div>
        );
    }
}