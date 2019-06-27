import React, {Component} from 'react'

import {Link}       from "react-router-dom";
import {Entity}     from "../../../../model/entity/Entity";

type ContextMenuProps = {
    onStart: () => void,
    action: (action: string) => void
    parent: Entity
};

export default class ContextMenu extends Component<ContextMenuProps> {
    state = {
        downloadUrl: `localhost://8080/${this.props.parent.type.toLowerCase()}/${this.props.parent.path.replace(/[\\]/g,"%2F")}/download`
    };

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
                <Link to="/rename">
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
                <Link to="/element-info">
                    <div className="context-menu-icon">
                        <i className="fas fa-info"/>
                    </div> 
                    <span>Info</span> 
                </Link>
            </div>
        );
    }
}