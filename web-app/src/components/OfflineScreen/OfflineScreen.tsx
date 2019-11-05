import React, { SFC } from "react";

export const OfflineScreen : SFC<{ img : string, text : string }> = props => (
    <div style={{ height : "100%", background : "#181818" }}>
        <div style={{
            position : "absolute",
            left: 0,
            right: 0,
            textAlign: "center",
            fontSize: 30,
            color: "white",
            margin: 0,
            marginTop: 40
        }}>
            <span>{props.text}</span>
        </div>
        <div style={{
            position: "absolute",
            width: "40%",
            top: "10%",
            left: 0,
            right: 0,
            margin: "auto"
        }}>
            <div style={{
                margin: "auto",
                textAlign: "center"
            }}>
                <div>
                    <img src={props.img} alt="..." />
                </div>
                <div style={{ height: 50 }}>
                    {props.children}
                </div>
            </div>
        </div>
    </div>
);