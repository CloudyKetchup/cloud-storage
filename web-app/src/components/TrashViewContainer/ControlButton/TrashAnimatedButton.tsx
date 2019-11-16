import React, { Component } from "react";

import { CSSProperties }    from "@material-ui/styles";
import { EntityHelpers }    from "../../../helpers";
import { Link }             from "react-router-dom";

type TrashItemControlButtonStyle = {
    hoverColor      : string
    hoverHeight?    : string
    foregroundStyle?: CSSProperties
    backgroundStyle?: CSSProperties
};

type IProps = {
    id?     : string
    class?  : string
    icon?   : JSX.Element
    text?   : string
    title?  : string
    color   : string
    style   : TrashItemControlButtonStyle
    isHoverable? : boolean
    isDisabled?  : boolean
    link?   : string
    onClick : (...args : any) => any
};

export default class TrashAnimatedButton extends Component<IProps> {

    ref : HTMLElement | null = null;

    componentDidMount = () => {
        if (this.props.id) {
            this.ref = document.getElementById(this.props.id);

            if (this.ref) {
                this.ref.addEventListener("mouseover", this.hover);
                this.ref.addEventListener("mouseout", this.unHover);
            }
        }
    };

    componentWillUnmount = () => {
        if (this.ref) {
            this.ref.removeEventListener("mouseover", this.hover);
            this.ref.removeEventListener("mouseout", this.unHover);
        }
    };

    hover = async () => {
        if (this.ref) {
            this.ref.style.transform = 
                this.props.isHoverable
                ?
                `translateY(-${this.props.style.hoverHeight})`
                :
                this.props.style.foregroundStyle
                &&
                this.props.style.foregroundStyle.transform
                ||
                "";
            this.ref.style.background = this.props.style.hoverColor;
        }
    };

    unHover = async () => {
        if (this.ref) {
            this.ref.style.transform = 
                this.props.style.foregroundStyle
                &&
                this.props.style.foregroundStyle.transform
                ||
                "";
            this.ref.style.background = this.props.color;
        }
    };

    static Builder = class {
        props : IProps = {
            id      : EntityHelpers.uuidv4(),
            color   : "white",
            onClick : () => {},
            style   : {
                hoverColor : "",
                hoverHeight : "",
            }
        };

        isHoverable = () => {
            this.props.isHoverable = true;
            return this;
        };
        
        isDisabled = (disabled : boolean = true) => {
            this.props.isDisabled = disabled;
            return this;
        };

        isLink = (link : string) => {
            this.props.link = link;
            return this;
        };

        color = (color : string) => {
            this.props.color = color;
            return this;
        };

        icon = (icon : JSX.Element) => {
            this.props.icon = icon;
            return this;
        };

        title = (title : string) => {
            this.props.title = title;
            return this;
        };

        style = (style : TrashItemControlButtonStyle) => {
            this.props.style = style;
            return this;
        };

        onClick = (click : (...args : any) => void) => {
            this.props.onClick = click;
            return this;
        };

        build = () => (
            <TrashAnimatedButton
                id={this.props.id}
                color={this.props.color}
                text={this.props.text}
                isHoverable={this.props.isHoverable}
                isDisabled={this.props.isDisabled}
                link={this.props.link}
                title={this.props.title}
                onClick={this.props.onClick}
                style={this.props.style}
            >
                {this.props.icon}
                {this.props.text}
            </TrashAnimatedButton>
        );
    };

    buttonRender = () => {
        if (this.props.style.foregroundStyle && this.props.isDisabled)
            this.props.style.foregroundStyle.background = "gray";

        if (this.props.style.backgroundStyle && this.props.link) {
            this.props.style.backgroundStyle.marginTop = "10px";
        }

        return (
            <button
                title={this.props.title}
                onClick={this.props.onClick}
                id={this.props.id}
                style={this.props.style.foregroundStyle}
                disabled={this.props.isDisabled}
            >
                {this.props.children}
            </button>
        );
    };

    render = () => (
        this.props.link
        ?
        <Link to={this.props.link} style={this.props.style.backgroundStyle}>
            {this.buttonRender()}
        </Link>
        :
        <div className={this.props.class} style={this.props.style.backgroundStyle}>
            {this.buttonRender()}
        </div>
    );
}