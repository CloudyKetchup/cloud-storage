import React, { Component } from "react";

import { Entity }               from "../../../model/entity/Entity";
import { EntityType }           from "../../../model/entity/EntityType";
import { FileEntity }           from "../../../model/entity/FileEntity";
import { FileExtension }        from "../../../model/entity/FileExtension";
import { API_URL, FileHelpers, APIHelpers as API } from "../../../helpers";

import "./trash-item-info-container.css"
import TrashAnimatedButton from "../ControlButton/TrashAnimatedButton";
import { FolderEntity } from "../../../model/entity/FolderEntity";

export const TrashEmptyInfoContainer = () => (
    <div style={{ padding : "10%" }} className="trash-empty-info-container">
        <div style={{ width : "60%", margin : "auto" }}>
            <img src="https://image.flaticon.com/icons/svg/227/227010.svg" alt={"..."}/>
        </div>
        <div
            style={{
                fontFamily: "sans-serif",
                marginTop: "10%",
                fontSize: 20,
                textAlign: "center",
                animationName : "unset"
            }}
        >
            <span>Select an item for additional info</span>
        </div>
    </div>
);

export default class TrashItemInfoContainer extends Component<{ data : Entity }> {
    icon = () : JSX.Element => {
        if (this.props.data.type === EntityType.FILE) {
            const fileData = this.props.data as FileEntity;

            if (fileData.isMedia) {
                if (FileHelpers.imageAssign(fileData)) {
                    return <img src={`${API_URL}/file/${fileData.id}/${fileData.extension === FileExtension.IMAGE_GIF ? "image" : "thumbnail"}`} alt={"..."}/>;
                }
                return <i className="fas fa-image"/>;
            }
        } else if (this.props.data.type === EntityType.FOLDER) {
            return <i className="fas fa-folder"/>;
        }
        return <i className="fas fa-file"/>;
    };

    download = () => {
        const data = this.props.data;

        if (data.type === EntityType.FILE) {
            API.downloadFile(data.path, data.name);
        } else if (data.type === EntityType.FOLDER) {
            API.downloadFolder(data as FolderEntity);
        }
    };

    render = () => (
        <div className="trash-item-info-container">
            <div className="trash-item-info-header">
                <div
                    style={{
                        background: FileHelpers.imageAssign(this.props.data as FileEntity) ? "unset" : "#181818",
                        width: "70%",
                        fontSize: "50px",
                        color: "white",
                        borderRadius : 5
                    }}
                    className="trash-item-icon-wrapper"
                >
                    {this.icon()}
                </div>
                <div style={{ width : "fit-content", margin: "auto", marginTop: 10 }}>
                    <span>{this.props.data.name}</span>
                </div>
            </div>
            <div className="trash-item-info">
                <div>
                    <span>Time created</span>
                    <span>{this.props.data.timeCreated}</span>
                </div>
                <div>
                    <span>Size</span>
                    <span>{this.props.data.size}</span>
                </div>
                <div>
                    <span>Location</span>
                    <span>{this.props.data.location}</span>
                </div>
                <div>
                    <span>Type</span>
                    <span>
                        {
                            this.props.data.type === EntityType.FOLDER
                            ?
                            "Folder"
                            :
                            (this.props.data as FileEntity).isMedia
                            ?
                            "Media File"
                            :
                            "File"
                        }
                    </span>
                </div>
                {
                    this.props.data.type === EntityType.FILE
                    &&
                    (this.props.data as FileEntity).isMedia
                    &&
                    <div>
                        <span>Resolution</span>
                        <span>...</span>
                    </div>
                }
            </div>
            {
                new TrashAnimatedButton.Builder()
                    .color("white")
                    .icon(<i className="fas fa-cloud-download-alt"/>)
                    .isHoverable()
                    .style({
                        hoverColor : "white",
                        hoverHeight : "5px",
                        foregroundStyle : {
                            background : "white",
                            border : "1px solid gray",
                            width : "50px",
                            height : "40px",
                            fontSize : 15,
                            borderRadius : 5
                        },
                        backgroundStyle : {
                            background : "#181818",
                            height : "fit-content",
                            width : "fit-content",
                            borderRadius : 5,
                            margin : "auto"
                        }
                    })
                    .onClick(this.download)
                    .build()
            }
        </div>
    );
}
