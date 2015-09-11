package org.mellowtech.gapi.model

//import com.google.api.client.util.DateTime

case class SThumbnail(
  decodeImage: Option[Array[Byte]] = None, image: Option[String] = None, mimeType: Option[String] = None)

case class SUser(displayName: Option[String] = None, emailAddress: Option[String] = None,
                 isAuthenticatedUser: Option[Boolean] = None, kind: Option[String] = None, permissionId: Option[String] = None)

case class SParentReference(id: Option[String] = None, isRoot: Option[Boolean] = None, kind: Option[String] = None,
                            parentLink: Option[String] = None, selfLink: Option[String] = None)

case class SPermission(
  additionalRoles: Option[Seq[String]] = None, authKey: Option[String] = None, domain: Option[String] = None,
  emailAddress: Option[String] = None, etag: Option[String] = None, id: Option[String] = None,
  kind: Option[String] = None, name: Option[String] = None, photoLink: Option[String] = None,
  role: Option[String] = None, selfLink: Option[String] = None, stype: Option[String] = None,
  value: Option[String] = None, withLink: Option[Boolean] = None)

case class SProperty(
  etag: Option[String] = None, key: Option[String] = None, kind: Option[String] = None,
  selfLink: Option[String] = None, value: Option[String] = None, visibility: Option[String] = None)

case class SLabels(
  restricted: Option[Boolean] = None, starred: Option[Boolean] = None,
  trashed: Option[Boolean] = None, viewed: Option[Boolean] = None)

case class SIndexableText(text: Option[String])

case class SLocation(altitude: Option[Double] = None, latitude: Option[Double] = None, longitude: Option[Double] = None)

case class SImageMediaMetadata(
  aperture: Option[Float] = None, cameraMake: Option[String] = None, colorSpace: Option[String] = None, date: Option[String] = None,
  cameraModel: Option[String] = None, exposureBias: Option[Float] = None, exposureTime: Option[Float] = None,
  exposureMode: Option[String] = None, flashUsed: Option[Boolean] = None, focalLength: Option[Float] = None,
  height: Option[Int] = None, isoSpeed: Option[Int] = None, lens: Option[String] = None, location: Option[SLocation] = None,
  maxApertureValue: Option[Float] = None, meteringMode: Option[String] = None, rotation: Option[Int] = None,
  sensor: Option[String] = None, subjectDistance: Option[Int] = None,
  whiteBalance: Option[String] = None, width: Option[Int] = None)


case class SVideoMediaMetadata(durationMillis: Option[Long] = None, height: Option[Int] = None, width: Option[Int] = None)

case class SChildReference(
    childLink: Option[String] = None, id: Option[String] = None, kind: Option[String] = None, selfLink: Option[String] = None
)

/**
 * @author msvens
 */
case class SFile(
                  alternateLink: Option[String] = None,
                  appContent: Option[Boolean] = None,
                  copyable: Option[Boolean] = None,
                  createDate: Option[String] = None,
                  defaultOpen: Option[String] = None,
                  description: Option[String] = None,
                  downloadUrl: Option[String] = None,
                  editable: Option[Boolean] = None,
                  embedLink: Option[String] = None,
                  eTag: Option[String] = None,
                  explicitTrash: Option[Boolean] = None,
                  exportLinks: Option[Map[String, String]] = None,
                  fileExtension: Option[String] = None,
                  fileSize: Option[Long] = None,
                  folderColor: Option[String] = None,
                  headRevisionId: Option[String] = None,
                  iconLink: Option[String] = None,
                  id: Option[String] = None,
                  imageMediaMetadata: Option[SImageMediaMetadata] = None,
                  indexableText: Option[SIndexableText] = None,
                  kind: Option[String] = None,
                  labels: Option[SLabels] = None,
                  lastModifyingUser: Option[SUser] = None,
                  lastModifyingUserName: Option[String] = None,
                  lastViewedByMeDate: Option[String] = None,
                  markedViewedByMeDate: Option[String] = None,
                  md5CheckSum: Option[String] = None,
                  mimeType: Option[String] = None,
                  modifiedByMeDate: Option[String] = None,
                  modifiedDate: Option[String] = None,
                  openWithLinks: Option[Map[String, String]] = None,
                  originalFileName: Option[String] = None,
                  ownerNames: Option[Seq[String]] = None,
                  owners: Option[Seq[SUser]] = None,
                  parents: Option[Seq[SParentReference]] = None,
                  permissions: Option[Seq[SPermission]] = None,
                  properties: Option[Seq[SProperty]] = None,
                  quotaBytesUsed: Option[Long] = None,
                  selfLink: Option[String] = None,
                  shared: Option[Boolean] = None,
                  sharedWithMeDate: Option[String] = None,
                  thumbnail: Option[SThumbnail] = None,
                  sharingUser: Option[SUser] = None,
                  thumbnailLink: Option[String] = None,
                  title: Option[String] = None,
                  userPermission: Option[SPermission] = None,
                  version: Option[Long] = None,
                  videoMediaMetadata: Option[SVideoMediaMetadata] = None,
                  webContentLink: Option[String] = None,
                  webViewLink: Option[String] = None,
                  writersCanShare: Option[Boolean] = None
                  )


object ModelConversions {
  import com.google.api.services.drive.model._

  import scala.collection.JavaConverters._
  
  implicit def asSChildReference(cr: ChildReference) = SChildReference(
    childLink = Option(cr.getChildLink), id = Option(cr.getId), kind = Option(cr.getKind), selfLink = Option(cr.getSelfLink)    
  )

  implicit def asVideoMediaMetadata(v: File.VideoMediaMetadata) = SVideoMediaMetadata(
    durationMillis = if(v.getDurationMillis == null) None else Some(v.getDurationMillis), 
    height = if(v.getHeight == null) None else Some(v.getHeight), width = if(v.getWidth == null) None else Some(v.getWidth))

  implicit def asSLocation(l: File.ImageMediaMetadata.Location) = SLocation(
    altitude = if(l.getAltitude == null) None else Some(l.getAltitude), 
    latitude = if(l.getLatitude == null) None else Some(l.getLatitude), 
    longitude = if(l.getLongitude == null) None else Some(l.getLongitude))

  implicit def asSImageMediaMetadata(m: File.ImageMediaMetadata) = SImageMediaMetadata(
    aperture = if(m.getAperture == null) None else Some(m.getAperture), cameraMake = Option(m.getCameraMake), colorSpace = Option(m.getColorSpace),
    date = Option(m.getDate), cameraModel = Option(m.getCameraModel), exposureBias = if(m.getExposureBias == null) None else Some(m.getExposureBias),
    exposureTime = if(m.getExposureTime == null) None else Some(m.getExposureTime), exposureMode = Option(m.getExposureMode), 
    flashUsed = if(m.getFlashUsed == null) None else Some(m.getFlashUsed), focalLength = if(m.getFocalLength == null) None else Some(m.getFocalLength), 
    height = if(m.getHeight == null) None else Some(m.getHeight), isoSpeed = if(m.getIsoSpeed == null) None else Some(m.getIsoSpeed),
    lens = Option(m.getLens), location = if(m.getLocation == null) None else Some(m.getLocation), 
    maxApertureValue = if(m.getMaxApertureValue == null) None else Some(m.getMaxApertureValue), meteringMode = Option(m.getMeteringMode), 
    rotation = if(m.getRotation == null) None else Some(m.getRotation), sensor = Option(m.getSensor),
    subjectDistance = if(m.getSubjectDistance == null) None else Some(m.getSubjectDistance), whiteBalance = Option(m.getWhiteBalance), 
    width = if(m.getWidth == null) None else Some(m.getWidth))

  implicit def asSIndexableText(it: File.IndexableText) = SIndexableText(text = Option(it.getText))

  implicit def asSLabels(l: File.Labels) = SLabels(
    restricted = if(l.getRestricted == null) None else Some(l.getRestricted), 
    starred = if(l.getStarred == null) None else Some(l.getStarred),
    trashed = if(l.getTrashed == null) None else Some(l.getTrashed), 
    viewed = if(l.getViewed == null) None else Some(l.getViewed))

  implicit def asSThumbNail(t: File.Thumbnail) = SThumbnail(
    decodeImage = Option(t.decodeImage()), image = Option(t.getImage), mimeType = Option(t.getMimeType))

  implicit def asSProperty(p: Property): SProperty = SProperty(
    etag = Option(p.getEtag), key = Option(p.getKey), kind = Option(p.getKind),
    selfLink = Option(p.getSelfLink), value = Option(p.getValue), visibility = Option(p.getVisibility))

  implicit def asSPermission(p: Permission): SPermission = SPermission(
    additionalRoles = if (p.getAdditionalRoles == null) None else Some(p.getAdditionalRoles.asScala),
    authKey = Option(p.getAuthKey), domain = Option(p.getDomain), emailAddress = Option(p.getEmailAddress),
    etag = Option(p.getEtag), id = Option(p.getId), kind = Option(p.getKind), name = Option(p.getName),
    photoLink = Option(p.getPhotoLink), role = Option(p.getRole), selfLink = Option(p.getSelfLink),
    stype = Option(p.getType), value = Option(p.getValue), withLink = if(p.getWithLink == null) None else Some(p.getWithLink))

  implicit def asSParentReference(pr: ParentReference) = SParentReference(
    id = Option(pr.getId), isRoot = if(pr.getIsRoot == null) None else Some(pr.getIsRoot), kind = Option(pr.getKind),
    parentLink = Option(pr.getParentLink), selfLink = Option(pr.getSelfLink))

  implicit def asSUser(u: User): SUser = {
    SUser(
      displayName = Option(u.getDisplayName),
      emailAddress = Option(u.getEmailAddress),
      isAuthenticatedUser = if(u.getIsAuthenticatedUser == null) None else Some(u.getIsAuthenticatedUser),
      kind = Option(u.getKind),
      permissionId = Option(u.getPermissionId))
  }

  implicit def asSFile(f: File): SFile = {
    SFile(
      alternateLink = Option(f.getAlternateLink),
      appContent = if(f.getAppDataContents == null) None else Some(f.getAppDataContents),
      copyable = if(f.getCopyable == null) None else Some(f.getCopyable),
      createDate = if(f.getCreatedDate == null) None else Some(f.getCreatedDate.toStringRfc3339),
      defaultOpen = Option(f.getDefaultOpenWithLink),
      description = Option(f.getDescription),
      downloadUrl = Option(f.getDownloadUrl),
      editable = if(f.getEditable == null) None else Some(f.getEditable),
      embedLink = Option(f.getEmbedLink),
      eTag = Option(f.getEtag),
      explicitTrash = if(f.getExplicitlyTrashed == null) None else Some(f.getExplicitlyTrashed),
      fileExtension = Option(f.getFileExtension),
      exportLinks = { if (f.getExportLinks == null) None else Some(f.getExportLinks.asScala.toMap) },
      fileSize = if(f.getFileSize == null) None else Some(f.getFileSize),
      folderColor = Option(f.getFolderColorRgb),
      headRevisionId = Option(f.getHeadRevisionId),
      iconLink = Option(f.getIconLink),
      id = Option(f.getId),
      imageMediaMetadata = if(f.getImageMediaMetadata == null) None else Some(f.getImageMediaMetadata),
      indexableText = if(f.getIndexableText == null) None else Some(f.getIndexableText),
      kind = Option(f.getKind),
      labels = if(f.getLabels == null) None else Some(f.getLabels),
      lastModifyingUser = if(f.getLastModifyingUser == null) None else Some(f.getLastModifyingUser),
      lastModifyingUserName = Option(f.getLastModifyingUserName),
      lastViewedByMeDate = if(f.getLastViewedByMeDate == null) None else Some(f.getLastViewedByMeDate.toStringRfc3339),
      markedViewedByMeDate = if(f.getMarkedViewedByMeDate == null) None else Some(f.getMarkedViewedByMeDate.toStringRfc3339),
      md5CheckSum = Option(f.getMd5Checksum),
      mimeType = Option(f.getMimeType),
      modifiedByMeDate = if(f.getModifiedByMeDate == null) None else Some(f.getModifiedByMeDate.toStringRfc3339),
      modifiedDate = if(f.getModifiedDate == null) None else Some(f.getModifiedDate.toStringRfc3339),
      openWithLinks = if (f.getOpenWithLinks == null) None else Some(f.getOpenWithLinks.asScala.toMap),
      originalFileName = Option(f.getOriginalFilename),
      ownerNames = if (f.getOwnerNames == null) None else Some(f.getOwnerNames.asScala),
      owners = if (f.getOwners == null) None else Some(f.getOwners.asScala.map(asSUser(_))),
      parents = if (f.getParents == null) None else Some(f.getParents.asScala.map(asSParentReference(_))),
      permissions = if (f.getPermissions == null) None else Some(f.getPermissions.asScala.map(asSPermission(_))),
      properties = if (f.getProperties == null) None else Some(f.getProperties.asScala.map(asSProperty(_))),
      quotaBytesUsed = if(f.getQuotaBytesUsed == null) None else Some(f.getQuotaBytesUsed),
      selfLink = Option(f.getSelfLink),
      shared = if(f.getShared == null) None else Some(f.getShared),
      sharedWithMeDate = if(f.getSharedWithMeDate == null) None else Some(f.getSharedWithMeDate.toStringRfc3339),
      sharingUser = if(f.getSharingUser == null) None else Some(f.getSharingUser),
      thumbnail = if(f.getThumbnail == null) None else Some(f.getThumbnail),
      thumbnailLink = Option(f.getThumbnailLink),
      title = Option(f.getTitle),
      userPermission = if(f.getUserPermission == null) None else Some(f.getUserPermission),
      version = if(f.getVersion == null) None else Some(f.getVersion),
      videoMediaMetadata = if(f.getVideoMediaMetadata == null) None else Some(f.getVideoMediaMetadata),
      webContentLink = Option(f.getWebContentLink),
      webViewLink = Option(f.getWebViewLink),
      writersCanShare = if(f.getWritersCanShare == null) None else Some(f.getWritersCanShare)
    )
  }

}