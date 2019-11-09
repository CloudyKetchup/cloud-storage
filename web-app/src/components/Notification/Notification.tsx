import React, { Component } from "react";

import { NotificationEntity }       from "../../model/notification/NotificationEntity";
import { AppNotificationContext }   from "../../App";

export interface NotificationState {
    expanded    : boolean
    visibleText : string
    time        : number
    start       : number
}

export interface NotificationProps {
    id          : string
    data        : NotificationEntity
    autoDestroy?: boolean
    destroyTime?: number
    expandable  : boolean
    style?      : {}
}

export default class Notification<P extends NotificationProps> extends Component<P> {
    state : NotificationState = {
        expanded    : false,
        visibleText : "",
        time        : 0,
        start       : 0
    };

    componentDidMount = () => {
        this.setState({ visibleText : this.shortText() });

        this.props.autoDestroy
        &&
        setTimeout(() => {
            this.selfDestroy();
        }, this.props.destroyTime || 5000);
    };

    private selfDestroy = () => {
        const div = document.getElementById(this.props.id);

        if (div) {
            div.style.marginBottom = "10px";
            div.style.opacity = "0";
        }
        setTimeout(() => {
            AppNotificationContext.deleteNotification(this.props.data.id);
        }, 100);
    };

    shortText = () => {
        const text = this.props.data.text;

        return text.length > 33 ? `${text.substring(0, 33)} ...` : text;
    };

    text = (text : string, style : {} = { width : "auto", overflowY : "auto" }) => (
        <div style={style}>
            <span style={{ wordBreak : "break-all" }}>{text}</span>
        </div>
    );

    closeButton = (onClick? : () => void, style : {} = { width : this.props.data.text.length > 25 ? "7%" : "10%", marginLeft : "auto" }) => (
        <div style={style}>
            <button onClick={onClick || this.selfDestroy} style={{ color : "white" }}>
                <i className="fas fa-times"/>
            </button>
        </div>
    );

    expandButton = () => (
        this.props.data.text.length > 25
        &&
        <div onClick={() => this.setState({
            expanded    : !this.state.expanded,
            visibleText : !this.state.expanded ? this.props.data.text : this.shortText()
        })}>
            <i style={{ transform: this.state.expanded ? "rotate(180deg)" : "" }} className="fas fa-chevron-up"/>
        </div>
    );

    render = () => (
        <div style={this.props.style} id={this.props.id} className="notification">
            {this.props.children}
            {this.text(this.state.visibleText)}
            {this.props.expandable && this.expandButton()}
            {this.closeButton()}
        </div>
    );
}